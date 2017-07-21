package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

import scala.util.control.NonFatal

case class WaveformUrl(label: String, json: String, png: String)

object WaveformUrl {
  implicit val reads: Reads[WaveformUrl] = Json.reads[WaveformUrl]
}

class MediaServiceUrlGenClient(jsonClient: JsonClient) {
  def waveformUrls(maybeUid: Option[String]): Future[Seq[WaveformUrl]] = maybeUid match {
    case Some(uid) => waveformUrls(Seq(uid)).map(_.get(uid).getOrElse(Seq.empty))
    case None => Future.value(Seq.empty)
  }

  def waveformUrls(uids: Seq[String]): Future[Map[String, Seq[WaveformUrl]]] = uids match {
    case Nil => Future.value(Map.empty)
    case uid => getUrls(uid)
  }

  private def getUrls(uids: Seq[String]): Future[Map[String, Seq[WaveformUrl]]] = {
    jsonClient.get(Path() / "waveforms", Params("uid" -> uids), Headers.empty).map { response =>
      response.status match {
        case Status.Ok => parseUrls(Json.parse(response.contentString))
        case _ => Map.empty[String, Seq[WaveformUrl]]
      }
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
