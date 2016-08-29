package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Urn.format
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json.JsValue

class StitchClient(jsonClient: JsonClient) {
  def countsForTrack(session: UserSession, trackUrn: Urn, userUrn: Urn): Future[StitchCounts] = {
    val key = s"${userUrn.getIdentifier}|${trackUrn.getIdentifier}"

    val params = Params(
      "p" -> pathFor("p", key),
      "d" -> pathFor("d", key),
      "l" -> pathFor("l", key),
      "c" -> pathFor("c", key)
    )

    jsonClient.get(session, Path() / "bulk", params).map {
      case JsonResponse(OkStatus, body, _, _) => parseBody(body, key)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  private def pathFor(cat: String, key: String) =
    s"/ts?c=$cat.o.t&r=a&k=$key"

  private def parseBody(body: JsValue, key: String): StitchCounts = {
    val parseFn = parseCountFromCatBody(key) _
    StitchCounts(
      playback_count = parseFn(body \ "p"),
      download_count = parseFn(body \ "d"),
      favoritings_count = parseFn(body \ "l"),
      comment_count = parseFn(body \ "c")
    )
  }

  private def parseCountFromCatBody(key: String)(catBody: JsValue): Int =
    ((catBody \ key \ "series")(0) \ "count").as[Int]
}
