package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

class TracksClient(jsonClient: JsonClient) {
  def streamUrl(session: UserSession, streamRequest: StreamRequest): Future[StreamResponse] = {
    val requestBody = Json.stringify(Json.toJson(streamRequest))

    jsonClient
      .postWithSession(session, Path() / "track" / "stream", Params.empty, Headers.empty, Some(requestBody))
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[StreamUrlResponse]
          case Status.Unauthorized => StreamErrorResponse
          case Status.NotFound => StreamErrorResponse
          case _ => throw UnhandledResponseException(response)
        }
      }
  }

  def previewUrl(session: UserSession, streamRequest: StreamRequest): Future[StreamResponse] = {
    val requestBody = Json.stringify(Json.toJson(streamRequest))

    jsonClient
      .postWithSession(session, Path() / "track" / "preview", Params.empty, Headers.empty, Some(requestBody))
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[StreamUrlResponse]
          case Status.Unauthorized => StreamErrorResponse
          case Status.NotFound => StreamErrorResponse
          case _ => throw UnhandledResponseException(response)
        }
      }
  }

  def downloadUrl(session: UserSession, downloadRequest: DownloadRequest): Future[DownloadResponse] = {
    val requestBody = Json.stringify(Json.toJson(downloadRequest))

    jsonClient
      .postWithSession(session, Path() / "track" / "download", Params.empty, Headers.empty, Some(requestBody))
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[DownloadUrlResponse]
          case Status.Forbidden => DownloadErrorResponse
          case Status.NotFound => DownloadErrorResponse
          case _ => throw UnhandledResponseException(response)
        }
      }
  }
}
