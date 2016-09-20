package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.Json

class MediaServiceUrlGenClientSpec extends UnitSpecification {

  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
    def resultT = Await.result(resultF.liftToTry)
  }

  trait Context extends GenericContext[Seq[WaveformUrl]] {

    private def urlJson(label: String, uid: String) = {
      Json.obj(
        "label" -> label,
        "json" -> s"https://foo.sndcdn.com/$label/$uid.json",
        "png" -> s"https://bar.sndcdn.com/$label/$uid.png"
      )
    }

    val jsonClient = mock[JsonClient]
    val client = new MediaServiceUrlGenClient(jsonClient)
    val uid = "a1b2c3"
    val session = anonymousSession
    def response = Future.value(
      JsonResponse(OkStatus, Json.obj(
        "response" -> Json.arr(Json.obj(
          "uid" -> uid,
          "urls" -> Json.arr(urlJson("stream", uid), urlJson("preview", uid))
        )
      )))
    )
    when(jsonClient.get(beTypedEqualTo(session), beTypedEqualTo(Path() / "waveforms"),
      beTypedEqualTo(Params("uid" -> uid)), any)).thenReturn(response)
  }

  "waveformUrls" >> {
    "returns an array of waveform URL objects" in new Context {
      def resultF = client.waveformUrls(session, Some(uid))

      result must contain(
        WaveformUrl("stream",
          "https://foo.sndcdn.com/stream/a1b2c3.json",
          "https://bar.sndcdn.com/stream/a1b2c3.png"))
      result must contain(
        WaveformUrl("preview",
          "https://foo.sndcdn.com/preview/a1b2c3.json",
          "https://bar.sndcdn.com/preview/a1b2c3.png"))
    }

    "returns an empty array when the UID is absent" in new Context {
      def resultF = client.waveformUrls(session, None)

      result must be(Seq.empty)
    }
  }
}
