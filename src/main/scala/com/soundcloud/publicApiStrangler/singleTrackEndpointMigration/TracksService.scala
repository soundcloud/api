package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TracksService(trackmetadataClient: TrackmetadataClient) {
  def track(session: UserSession, urn: Urn, secretToken: Option[String]): Future[Response] =
    trackmetadataClient.track(session, urn).map {
      case Some(track) =>
        if (isTrackAccessible(session, secretToken, track)) generateSingleTrackResponse(track) else generateNotFoundResponse
      case None =>
        generateNotFoundResponse
    }

  private def generateNotFoundResponse: Response = {
    val res = Response(Status.NotFound)
    res.setContentString("""{"errors":[{"error_message":"404 - Not Found"}]}""")
    res
  }

  private def generateSingleTrackResponse(track: Track) = {
    val singleTrackPublicApiRepresentation = new SingleTrackPublicApiRepresentation(
      "track",
      track.urn.getIdentifier.toLong,
      track.user_urn.getIdentifier.toLong)

    val contentString = Json.stringify(singleTrackPublicApiRepresentation)
    val contentLength = contentString.getBytes("UTF-8").length

    val res = Response()
    res.setContentString(Json.stringify(singleTrackPublicApiRepresentation))
    res.contentType = "application/json"
    res.contentLength = contentLength
    res
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
