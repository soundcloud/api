package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

case class TrackRequests(trackRequests: List[TrackRequest])

object TrackRequests {
  implicit val trackRequestWrites: Writes[TrackRequest] = Json.writes[TrackRequest]
  implicit val trackRequestsWrites: Writes[TrackRequests] = Json.writes[TrackRequests]
}

class TracksClient(jsonClient: JsonClient) {
  private val maxBatchSize = 300

  def visibleTracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    val batches = trackRequests.grouped(maxBatchSize).map { batch =>
      val requestParams = TracksParams(batch)
      val requestBody = Json.stringify(Json.toJson(requestParams))

      jsonClient
        .postWithSession(
          session,
          Path() / "tracks",
          Params.empty,
          Headers.empty,
          Some(requestBody)
        )
        .map { response =>
          response.status match {
            case Status.Ok => (Json.parse(response.contentString) \ "data").as[List[VisibleTrack]]
            case _ => throw UnhandledResponseException(response)
          }
        }
    }

    Future.collect(batches.toSeq).map(_.flatten.toList)
  }

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
