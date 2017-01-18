package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.ModuleConversions._
import com.soundcloud.jvmkit.module.httpclient.{Headers, HttpClient, HttpResponse, OkHttpStatus, Params}
import com.soundcloud.jvmkit.module.servicediscovery.Path
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, Json}

class MediaServiceUrlGenClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[HttpClient]
    val client = new MediaServiceUrlGenClient(jsonClient)

    val path = Path() / "waveforms"

    val session = anonymousSession

    val uid1 = "a1b2c3"
    val uid2 = "def456"

    def stubbedResponse(uids: Seq[String]): Future[HttpResponse] = {
      jsonClient.get(path, Params("uid" -> uids), Headers.empty)
    }

    def trackWaveformsObject(uid: String): JsObject = {
      Json.obj(
        "uid"  -> uid,
        "urls" -> Json.arr(
          waveformUrlObject("stream", uid),
          waveformUrlObject("preview", uid)
        )
      )
    }

    def waveformUrlObject(label: String, uid: String): JsObject = {
      Json.obj(
        "label" -> label,
        "json"  -> s"https://foo.sndcdn.com/$label/$uid.json",
        "png"   -> s"https://bar.sndcdn.com/$label/$uid.png"
      )
    }
  }

  "waveformUrls" >> {
    "returns an array of waveform URL objects for multiple tracks" in new Context {
      stubbedResponse(Seq(uid1, uid2)) returns Future.value(
        HttpResponse(OkHttpStatus, Json.obj(
          "response" -> Json.arr(
            trackWaveformsObject(uid1),
            trackWaveformsObject(uid2)
          )
        ).toString())
      )

      Await.result(client.waveformUrls(session, Seq(uid1, uid2))) must beLike {
        case Some(map) => {
          map.get(uid1) must beLike {
            case Some(list) => {
              list must contain(
                WaveformUrl("stream",
                  s"https://foo.sndcdn.com/stream/$uid1.json",
                  s"https://bar.sndcdn.com/stream/$uid1.png")
              )

              list must contain(
                WaveformUrl("preview",
                  s"https://foo.sndcdn.com/preview/$uid1.json",
                  s"https://bar.sndcdn.com/preview/$uid1.png")
              )
            }
          }

          map.get(uid2) must beLike {
            case Some(list) => {
              list must contain(
                WaveformUrl("stream",
                  s"https://foo.sndcdn.com/stream/$uid2.json",
                  s"https://bar.sndcdn.com/stream/$uid2.png")
              )

              list must contain(
                WaveformUrl("preview",
                  s"https://foo.sndcdn.com/preview/$uid2.json",
                  s"https://bar.sndcdn.com/preview/$uid2.png")
              )
            }
          }
        }
      }
    }

    "returns an array of waveform URL objects for a single track" in new Context {
      stubbedResponse(Seq(uid1)) returns Future.value(
        HttpResponse(OkHttpStatus, Json.obj(
          "response" -> Json.arr(
            trackWaveformsObject(uid1)
          )
        ).toString())
      )

      Await.result(client.waveformUrls(session, Some(uid1))) must beLike {
        case Some(list) => {
          list must contain(
            WaveformUrl("stream",
              s"https://foo.sndcdn.com/stream/$uid1.json",
              s"https://bar.sndcdn.com/stream/$uid1.png")
          )

          list must contain(
            WaveformUrl("preview",
              s"https://foo.sndcdn.com/preview/$uid1.json",
              s"https://bar.sndcdn.com/preview/$uid1.png")
          )
        }
      }
    }

    "returns an empty array when the UID is absent" in new Context {
      Await.result(client.waveformUrls(session, None)) must beLike { case Some(list) => list must beEmpty }
    }

    "returns None when failing" in new Context {
      stubbedResponse(Seq(uid1)) returns Future.exception(new RuntimeException("fail"))
      Await.result(client.waveformUrls(session, Some(uid1))) must beNone
    }
  }
}
