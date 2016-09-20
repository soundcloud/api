package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.Future
import play.api.libs.json.{JsArray, JsValue}

case class WaveformUrl(label: String, json: String, png: String)

class MediaServiceUrlGenClient(jsonClient: JsonClient) {
  def waveformUrls(session: UserSession, maybeUid: Option[String]): Future[Seq[WaveformUrl]] = maybeUid match {
    case None => Future.value(Seq.empty)
    case Some(uid) => jsonClient.get(session, Path() / "waveforms", Params("uid" -> uid)).map {
      case JsonResponse(OkStatus, body, _, _) => parseUrls(body)
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }

  private def parseUrls(json: JsValue): Seq[WaveformUrl] = {
    (json \ "response" \ "urls").as[JsArray].value.map { urlJson =>
      val label = (urlJson \ "label").as[String]
      val json = (urlJson \ "json").as[String]
      val png = (urlJson \ "png").as[String]
      WaveformUrl(label, json, png)
    }
  }
}
