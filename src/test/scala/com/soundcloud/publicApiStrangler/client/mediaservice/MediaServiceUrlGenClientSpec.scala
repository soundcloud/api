package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, Json}

class MediaServiceUrlGenClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val client = new MediaServiceUrlGenClient(jsonClient)

    val path = Path() / "waveforms"

    val session = anonymousSession
    val uid = "a1b2c3"
  }

  "waveformUrls" >> {
    "returns an array of waveform URL objects" in new Context {
      jsonClient.get(session, path, Params("uid" -> uid), Params.empty) returns Future.value(
        JsonResponse(OkStatus, Json.obj(
          "response" -> Json.arr(Json.obj(
            "uid" -> uid,
            "urls" -> Json.arr(
              waveformUrlObject("stream", uid),
              waveformUrlObject("preview", uid)
            ))
          )
        ))
      )

      val result = Await.result(client.waveformUrls(session, Some(uid)))

      result must contain(
        WaveformUrl("stream",
          "https://foo.sndcdn.com/stream/a1b2c3.json",
          "https://bar.sndcdn.com/stream/a1b2c3.png"))

      result must contain(
        WaveformUrl("preview",
          "https://foo.sndcdn.com/preview/a1b2c3.json",
          "https://bar.sndcdn.com/preview/a1b2c3.png"))

      private def waveformUrlObject(label: String, uid: String): JsObject = {
        Json.obj(
          "label" -> label,
          "json" -> s"https://foo.sndcdn.com/$label/$uid.json",
          "png" -> s"https://bar.sndcdn.com/$label/$uid.png"
        )
      }
    }

    "returns an empty array when the UID is absent" in new Context {
      Await.result(client.waveformUrls(session, None)) must beEmpty
    }
  }
}
