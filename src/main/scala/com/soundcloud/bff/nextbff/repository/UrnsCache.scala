package com.soundcloud.bff.nextbff.repository

import java.util.concurrent.TimeUnit.MINUTES

import com.soundcloud.bff.{Future, JsObject}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.json.Json

class UrnsCache(cache: Cache) {

  def get(urns: List[Urn],
          bulkFetch: Set[Urn] => Future[Map[Urn, JsObject]],
          cacheExpirationTimeMinutes: Int,
          cacheAllowed: JsObject => Boolean) =
    collectFromCache(urns).flatMap {
      fromCache =>

        val found = filterFoundUrns(fromCache.toList)
        val notFound = filterNotFoundUrns(fromCache.toList)

        fetchIfNeeded(notFound, bulkFetch).map {
          recovered =>
            val recoveredByUrn = recovered.toMap
            addUrnsToCache(recoveredByUrn, cacheAllowed, cacheExpirationTimeMinutes)
            mergeResults(urns, found, recoveredByUrn).flatten.toMap
        }
    }

  private def collectFromCache(urns: List[Urn]) = {
    val futures =
      for (urn <- urns) yield {
        cache.get(urn.getString).map {
          json =>
            urn -> json.map(Json.fromString).map(_.as[JsObject])
        }
      }
    Future.collect(futures)
  }

  private def fetchIfNeeded(notFound: List[Urn], bulkFetch: Set[Urn] => Future[Map[Urn, JsObject]]) =
    notFound match {
      case Nil =>
        Future.value(Nil)
      case other =>
        bulkFetch(notFound.toSet)
    }

  private def filterFoundUrns(result: List[(Urn, Option[JsObject])]) =
    result.collect {
      case (urn, Some(json)) =>
        urn -> json
    }.toMap

  private def filterNotFoundUrns(result: List[(Urn, Option[JsObject])]) =
    result.collect {
      case (urn, None) =>
        urn
    }

  private def addUrnsToCache(recoveredByUrn: Map[Urn, JsObject], cacheAllowed: JsObject => Boolean, cacheExpirationTimeMinutes: Int) =
    for ((urn, json) <- recoveredByUrn; if (cacheAllowed(json)))
      cache.set(
        urn.getString,
        Json.stringify(json),
        cacheExpirationTimeMinutes,
        MINUTES)

  private def mergeResults(urns: List[Urn],
                           found: Map[Urn, JsObject],
                           recovered: Map[Urn, JsObject]) =
    for (urn <- urns) yield {
      found.get(urn).orElse(recovered.get(urn)).map(urn -> _)
    }
}
