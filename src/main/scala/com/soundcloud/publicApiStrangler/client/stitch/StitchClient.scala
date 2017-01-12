package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.jvmkit.Urn.format
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.Path
import com.twitter.util.Future
import play.api.libs.json.JsValue

class StitchClient(jsonClient: JsonClient) {
  def countsForTrack(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[StitchCounts] = {
    countsForTracks(session, Set(trackUrn), userUrn).map(_(trackUrn))
  }

  def countsForTracks(session: UserSession, trackUrn: Set[Urn], userUrn: Urn): Future[Map[Urn, StitchCounts]] = {
    val keys = trackUrn.map(urn => s"${userUrn.getIdentifier}|${urn.getIdentifier}")
    val keyParam = keys.map(key => s"k=$key").mkString("&")

    jsonClient.get(session, Path() / "bulk", params(keyParam)).map {
      case JsonResponse(OkStatus, body, _, _) => parseBody(body, keys)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  def countsForTracks(session: UserSession, userToTrackUrn: Set[(Urn, Urn)]): Future[Map[Urn, StitchCounts]] = {
    val keys = userToTrackUrn.map(key => s"${key._1.getIdentifier}|${key._2.getIdentifier}")
    val keyParam = keys.map(key => s"k=$key").mkString("&")

    jsonClient.get(session, Path() / "bulk", params(keyParam)).map {
      case JsonResponse(OkStatus, body, _, _) => parseBody(body, keys)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  private def params(keyParam: String) = Params(
    "plays" -> pathFor("p", keyParam),
    "downloads" -> pathFor("d", keyParam),
    "likes" -> pathFor("l", keyParam),
    "comments" -> pathFor("c", keyParam),
    "reposts" -> pathFor("r", keyParam)
  )

  private def pathFor(cat: String, keyParam: String) =
    s"/ts?category=$cat.o.t&minus-category=n.$cat.o.t&resolution=alltime&$keyParam"

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
    ((catBody \ key \ "series")(0) \ "count").asOpt[Int].getOrElse(0)
}
