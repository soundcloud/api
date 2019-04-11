package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsLookupResult, Json}

class StitchClient(jsonClient: JsonClient) {
  def countsForTrack(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[StitchCounts] = {
    countsForTracksByUser(session, userUrn, Set(trackUrn)).map(_ (trackUrn))
  }

  def countsForTracksByUser(session: UserSession, userUrn: Urn, trackUrns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, StitchCounts]] = {
    inBatches(trackUrns, batchSize) { trackUrnBatch => {
      val keys = trackUrnBatch.map(urn => s"${userUrn.identifier}|${urn.identifier}")
      val keyParam = keys.map(key => s"k=$key").mkString("&")

      get(session, params(keyParam), keys)
    }
    }.map(_.flatten.toMap)
  }

  def countsForTracks(session: UserSession, userToTrackUrns: Set[(Urn, Urn)], batchSize: Int = 50): Future[Map[Urn, StitchCounts]] = {
    inBatches(userToTrackUrns, batchSize) { userToTrackUrnBatch => {
      val keys = userToTrackUrnBatch.map(key => s"${key._1.identifier}|${key._2.identifier}")
      val keyParam = keys.map(key => s"k=$key").mkString("&")

      get(session, params(keyParam), keys)
    }
    }.map(_.flatten.toMap)
  }

  private def get(session: UserSession, params: Params, keys: Set[String]) = {
    jsonClient.getWithSession(session, Path() / "bulk", params, Headers.empty()).map { response: Response =>
      response.status match {
        case Status.Ok => parseBody(response.contentString, keys)
        case _ => throw new RuntimeException("Unexpected response status")
      }
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

  private def parseBody[T](body: String, keys: Set[String]): Map[Urn, StitchCounts] = {
    keys.map { key =>
      val jsBody = Json.parse(body)
      val urnFromKey = Urn("soundcloud", "tracks", key.split("\\|").last)

      val parseFn = parseCountFromCatBody(key) _

      val count = StitchCounts(
        playback_count = parseFn(jsBody \ "plays"),
        download_count = parseFn(jsBody \ "downloads"),
        favoritings_count = parseFn(jsBody \ "likes"),
        comment_count = parseFn(jsBody \ "comments"),
        reposts_count = parseFn(jsBody \ "reposts")
      )

      (urnFromKey, count)
    }.toMap
  }

  private def parseCountFromCatBody(key: String)(catBody: JsLookupResult): Int =
    ((catBody \ key \ "series") (0) \ "count").asOpt[Int].getOrElse(0)
}
