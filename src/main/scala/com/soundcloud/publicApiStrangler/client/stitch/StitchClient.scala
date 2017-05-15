package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions.toBigJvmKitUserSession
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.Future
import play.api.libs.json.JsValue

class StitchClient(jsonClient: JsonClient) {
  def countsForTrack(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[StitchCounts] = {
    countsForTracksByUser(session, userUrn, Set(trackUrn)).map(_ (trackUrn))
  }

  def countsForTracksByUser(session: UserSession, userUrn: Urn, trackUrns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, StitchCounts]] = {
    inBatches(trackUrns, batchSize) { trackUrnBatch => {
      val keys = trackUrnBatch.map(urn => s"${userUrn.getIdentifier}|${urn.getIdentifier}")
      val keyParam = keys.map(key => s"k=$key").mkString("&")

      get(session, params(keyParam), keys)
    }
    }.map(_.flatten.toMap)
  }

  def countsForTracks(session: UserSession, userToTrackUrns: Set[(Urn, Urn)], batchSize: Int = 50): Future[Map[Urn, StitchCounts]] = {
    inBatches(userToTrackUrns, batchSize) { userToTrackUrnBatch => {
      val keys = userToTrackUrnBatch.map(key => s"${key._1.getIdentifier}|${key._2.getIdentifier}")
      val keyParam = keys.map(key => s"k=$key").mkString("&")

      get(session, params(keyParam), keys)
    }
    }.map(_.flatten.toMap)
  }

  private def get(session: UserSession, params: Params, keys: Set[String]) = {
    jsonClient.get(session, Path() / "bulk", params, Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => parseBody(body, keys)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  private def inBatches[A, B](urns: Set[A], batchSize: Int)(f: (Set[A] => Future[B])): Future[Seq[B]] = {
    Future.collect {
      urns.grouped(batchSize).map(f).toList
    }.map(_.toList)
  }

  private def params(keyParam: String) = Params(
    "plays" -> pathFor("p", keyParam, false),
    "downloads" -> pathFor("d", keyParam, false),
    "likes" -> pathFor("l", keyParam, true),
    "comments" -> pathFor("c", keyParam, true),
    "reposts" -> pathFor("r", keyParam, true)
  )

  private def pathFor(cat: String, keyParam: String, withMinusCategory: Boolean) = {
    val minusCategory = if (withMinusCategory) s"&minus-category=n.$cat.o.t" else ""
    s"/ts?category=$cat.o.t$minusCategory&resolution=alltime&$keyParam"
  }

  private def parseBody[T](body: JsValue, keys: Set[String]): Map[Urn, StitchCounts] = {
    keys.map { key =>
      val urnFromKey = Urn("soundcloud", "tracks", key.split("\\|").last)

      val parseFn = parseCountFromCatBody(key) _

      val count = StitchCounts(
        playback_count = parseFn(body \ "plays"),
        download_count = parseFn(body \ "downloads"),
        favoritings_count = parseFn(body \ "likes"),
        comment_count = parseFn(body \ "comments"),
        reposts_count = parseFn(body \ "reposts")
      )

      (urnFromKey, count)
    }.toMap
  }

  private def parseCountFromCatBody(key: String)(catBody: JsValue): Int =
    ((catBody \ key \ "series") (0) \ "count").asOpt[Int].getOrElse(0)
}
