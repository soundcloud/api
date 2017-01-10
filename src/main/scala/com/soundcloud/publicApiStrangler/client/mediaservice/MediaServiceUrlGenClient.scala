package com.soundcloud.publicApiStrangler.client.mediaservice

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{Error, Result, ServerError, Success}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.FinagleLoggerFactory
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{JsArray, JsValue}

case class WaveformUrl(label: String, json: String, png: String)

class MediaServiceUrlGenClient(jsonClient: JsonClient) {
  val logger = FinagleLoggerFactory.getLogger(this.getClass)

  def waveformUrls(session: UserSession, maybeUid: Option[String]): Future[Option[Seq[WaveformUrl]]] = maybeUid match {
    case None => Future.value(Some(Seq.empty))
    case Some(uid) => jsonClient.get(session, Path() / "waveforms", Params("uid" -> uid), Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => Some(parseUrls(body))
      case _ => None
    } handle { case NonFatal(ex) => None }
  }

  def waveformUrlsAsResult(session: UserSession, maybeUid: Option[String]): Future[Result[Seq[WaveformUrl]]] = maybeUid match {
    case None => Future.value(Success(Seq.empty))
    case Some(uid) => jsonClient.get(session, Path() / "waveforms", Params("uid" -> uid)).map {
      case JsonResponse(OkStatus, body, _, _) => Success(parseUrls(body))
      case _ => {
        logger.error("Error occured while fetching media URL")
        ServerError(Error("Something wrong with media service"))
      }
    }
  }

  private def parseUrls(json: JsValue): Seq[WaveformUrl] = {
    ((json \ "response")(0) \ "urls").as[JsArray].value.map { urlJson =>
      val label = (urlJson \ "label").as[String]
      val json = (urlJson \ "json").as[String]
      val png = (urlJson \ "png").as[String]
      WaveformUrl(label, json, png)
    }
  }
}
