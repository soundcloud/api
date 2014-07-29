package com.soundcloud.rollout

import java.util.concurrent.atomic.AtomicInteger
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.finagle.http.NotFoundStatus
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import com.twitter.util.Future
import play.api.libs.json.JsValue
import java.util.concurrent.TimeUnit
import com.soundcloud.bff.services.JsonService

class RolloutRepository(rolloutClient: JsonService, cache: Cache) {

  private val defaultPercentage = 0
  private val CacheExpirationTimeMinutes = 1
  private val counter = new AtomicInteger(-1)

  def activated(session: UserSession, featureName: String): Future[Boolean] =
    percentage(session, featureName).map { percentage =>
      counter.incrementAndGet % 100 < percentage
    }

  private def percentage(session: UserSession, featureName: String) =
    cache.get(featureName).flatMap {
      case Some(ParseInt(percentage)) =>
        Future(percentage)
      case None =>
        fetchPercentage(session, featureName)
          .onSuccess(percentage => cache.set(featureName, percentage.toString, CacheExpirationTimeMinutes, TimeUnit.MINUTES))
    }

  private def fetchPercentage(session: UserSession, featureName: String): Future[Int] =
    rolloutClient.get(session, Path("/features") / s"$featureName.json", Map(), Map()).map {
      case JsonResponse(OkStatus, body, _, _) => percentage(body).getOrElse(defaultPercentage)
      case JsonResponse(NotFoundStatus, _, _, _) => defaultPercentage
    }.rescue {
      case e => Future(defaultPercentage)
    }

  private def percentage(json: JsValue) =
    (json \ "percentages").as[List[Int]].headOption

  private object ParseInt {
    def unapply(string: String) =
      try Some(string.toInt)
      catch {
        case ex: NumberFormatException =>
          None
      }
  }
}
