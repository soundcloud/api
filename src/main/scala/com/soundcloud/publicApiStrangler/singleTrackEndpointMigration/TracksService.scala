package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TracksService(trackmetadataClient: TrackmetadataClient) {
  def track(session: UserSession, urn: Urn, secretToken: Option[String]): Future[Response] =
    trackmetadataClient.track(session, urn).map {
      case Some(track) if (isTrackAccessible(session, secretToken, track)) =>
        generateSingleTrackResponse(track)
      case _ =>
        generateNotFoundResponse
    }

  private def generateNotFoundResponse: Response = {
    val contentString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
    generateResponse(Status.NotFound, contentString)
  }

  private def generateSingleTrackResponse(track: Track): Response = generateResponse(Status.Ok, jsonForTrack(track))

  private def generateResponse(status: Status, content: String): Response = {
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(status)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def jsonForTrack(track: Track) = {
    val singleTrackPublicApiRepresentation = new TrackRepresentation(
      track = track,
      user_urn = track.user_urn)
    Json.stringify(singleTrackPublicApiRepresentation)
  }

  private def isTrackAccessible(session: UserSession, secretToken: Option[String], track: Track): Boolean =
    isPrivacyAuthorized(session, secretToken, track) && !isDisabled(track)

  private def isPrivacyAuthorized(session: UserSession, secretToken: Option[String], track: Track): Boolean = {
    track.public ||
      track.user_urn == session.getUser ||
      (secretToken.filter(_ == track.secret_token).isDefined)
  }

  private def isDisabled(track: Track): Boolean =
    track.disabled_at.isDefined
}
