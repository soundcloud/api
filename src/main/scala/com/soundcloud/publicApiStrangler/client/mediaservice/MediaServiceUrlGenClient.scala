package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.module.util.UserSession
import com.soundcloud.jvmkit.module.httpclient.{Headers, HttpClient, HttpResponse, OkHttpStatus, Params}
import com.soundcloud.jvmkit.module.servicediscovery.Path
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{JsArray, JsString, JsValue, Json, Reads}

case class WaveformUrl(label: String, json: String, png: String)

object WaveformUrl {
  implicit val reads: Reads[WaveformUrl] = Json.reads[WaveformUrl]
}

class MediaServiceUrlGenClient(jsonClient: HttpClient) {
  def waveformUrls(session: UserSession, maybeUid: Option[String]): Future[Option[Seq[WaveformUrl]]] = maybeUid match {
    case Some(uid) => waveformUrls(session, Seq(uid)).map(_.flatMap(_.get(uid)))
    case None => Future.value(Some(Seq.empty))
  }

  def waveformUrls(session: UserSession, uids: Seq[String]): Future[Option[Map[String, Seq[WaveformUrl]]]] = uids match {
    case Nil => Future.value(Some(Map.empty))
    case uid => getUrls(session, uid) handle { case NonFatal(ex) => None }
  }

  private def getUrls(session: UserSession, uids: Seq[String]): Future[Option[Map[String, Seq[WaveformUrl]]]] = {
    jsonClient.get(Path() / "waveforms", Params("uid" -> uids), Headers.empty).map {
      case HttpResponse(OkHttpStatus, body, _) => Some(parseUrls(Json.parse(body)))
      case _ => None
    }
  }

  private def parseUrls(json: JsValue): Map[String, Seq[WaveformUrl]] = {
    (json \ "response").as[JsArray].value.map {
      json => (json \ "uid").as[JsString].value -> (json \ "urls").as[Seq[WaveformUrl]]
    }.toMap
  }
}
