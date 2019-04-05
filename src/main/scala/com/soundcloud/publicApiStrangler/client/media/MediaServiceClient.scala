package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

case class Transcoding(uuid: String, mime_type: String)

object Transcoding {
  implicit val reads: Reads[Transcoding] = Json.reads[Transcoding]
}

class MediaServiceClient(jsonClient: JsonClient) {

  def fetchTranscodings(session: UserSession, uid: String): Future[List[Transcoding]] = {
    jsonClient.getWithSession(session, Path() / "transcodings", Params("upload_ids" -> uid), Headers.empty).map { response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "transcodings").as[Map[String, List[Transcoding]]].getOrElse(uid, List.empty)
        case _ => throw UnhandledResponseException(response)
      }
    }
  }

  def fetchStreamUrl(session: UserSession, transcodingUUID: String, protocol: String): Future[Option[String]] =
    fetchMediaUrl(session, transcodingUUID, "stream", protocol)

  def fetchPreviewUrl(session: UserSession, transcodingUUID: String, protocol: String): Future[Option[String]] =
    fetchMediaUrl(session, transcodingUUID, "preview", protocol)

  private def fetchMediaUrl(session: UserSession, transcodingUUID: String, routeType: String, protocol: String): Future[Option[String]] = {
    jsonClient.getWithSession(session, Path() / routeType / transcodingUUID / protocol, Params.empty, Headers.empty).map { response =>
      response.status match {
        case Status.Ok =>
          val json = Json.parse(response.contentString)
          Some((json \ "url").as[String])
        case Status.NotFound => None
        case _ => throw UnhandledResponseException(response)
      }
    }
  }

  def fetchDownloadOriginalUrl(session: UserSession, uid: String): Future[Option[String]] =
    jsonClient.getWithSession(session, Path() / "download-original" / uid, Params.empty, Headers.empty).map { response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "url").asOpt[String]
        case Status.NotFound => None
        case _ => throw UnhandledResponseException(response)
      }
    }
}
