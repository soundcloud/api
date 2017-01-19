package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{JsArray, JsString, JsValue, Json, Reads}

case class WaveformUrl(label: String, json: String, png: String)

object WaveformUrl {
  implicit val reads: Reads[WaveformUrl] = Json.reads[WaveformUrl]
}

class MediaServiceUrlGenClient(jsonClient: JsonClient) {
  def waveformUrls(session: UserSession, maybeUid: Option[String]): Future[Seq[WaveformUrl]] = maybeUid match {
    case Some(uid) => waveformUrls(session, Seq(uid)).map(_.get(uid).getOrElse(Seq.empty))
    case None => Future.value(Seq.empty)
  }

  def waveformUrls(session: UserSession, uids: Seq[String]): Future[Map[String, Seq[WaveformUrl]]] = uids match {
    case Nil => Future.value(Map.empty)
    case uid => getUrls(session, uid)
  }

  private def getUrls(session: UserSession, uids: Seq[String]): Future[Map[String, Seq[WaveformUrl]]] = {
    jsonClient.get(session, Path() / "waveforms", Params("uid" -> uids), Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => parseUrls(body)
      case _ => Map.empty[String, Seq[WaveformUrl]]
    }.handle {
      case NonFatal(e) => Map.empty[String, Seq[WaveformUrl]]
    }
  }

  private def parseUrls(json: JsValue): Map[String, Seq[WaveformUrl]] = {
    (json \ "response").as[JsArray].value.map {
      json => (json \ "uid").as[JsString].value -> (json \ "urls").as[Seq[WaveformUrl]]
    }.toMap
  }
}
