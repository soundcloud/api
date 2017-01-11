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

    val params = Params(
      "p" -> pathFor("p", keyParam),
      "d" -> pathFor("d", keyParam),
      "l" -> pathFor("l", keyParam),
      "c" -> pathFor("c", keyParam)
    )


    jsonClient.get(session, Path() / "bulk", params).map {
      case JsonResponse(OkStatus, body, _, _) => parseBody(body, keys)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  private def pathFor(cat: String, keyParam: String) =
    s"/ts?c=$cat.o.t&r=a&$keyParam"

  private def parseBody[T](body: JsValue, keys: Set[String]): Map[Urn, StitchCounts] = {
    keys.map { key =>
      val urnFromKey = Urn("soundcloud", "tracks", key.split("\\|").last)

      val parseFn = parseCountFromCatBody(key) _

      val count = StitchCounts(
        playback_count = parseFn(body \ "p"),
        download_count = parseFn(body \ "d"),
        favoritings_count = parseFn(body \ "l"),
        comment_count = parseFn(body \ "c")
      )

      (urnFromKey, count)
    }.toMap
  }

  private def parseCountFromCatBody(key: String)(catBody: JsValue): Int =
    ((catBody \ key \ "series")(0) \ "count").as[Int]
}
