package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.FinagleLoggerFactory
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{JsArray, JsValue}

case class WaveformUrl(label: String, json: String, png: String)

class MediaServiceUrlGenClient(jsonClient: JsonClient) {
  val logger = FinagleLoggerFactory.getLogger(this.getClass)

  def waveformUrls(session: UserSession, maybeUid: Option[String]): Future[Option[Seq[WaveformUrl]]] =
    waveformUrls(session, maybeUid.toList).map(_.flatMap(a => Some(a.values.headOption.getOrElse(Seq.empty))))

  def waveformUrls(session: UserSession, uids: Seq[String]): Future[Option[Map[String, Seq[WaveformUrl]]]] = uids match {
    case Nil => Future.value(Some(Map.empty))
    case uid => jsonClient.get(session, Path() / "waveforms", Params("uid" -> uid), Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => Some(Map(uid(0) -> parseUrls(body)))
      case _ => None
    } handle { case NonFatal(ex) => None }
  }

  private def parseUrls(json: JsValue): Seq[WaveformUrl] = {
    // TODO: Iterate over the list to build a map.
    ((json \ "response")(0) \ "urls").as[JsArray].value.map { urlJson =>
      val label = (urlJson \ "label").as[String]
      val json = (urlJson \ "json").as[String]
      val png = (urlJson \ "png").as[String]

      WaveformUrl(label, json, png)
    }
  }
}
