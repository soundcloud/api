package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TracksService(trackmetadataClient: TrackmetadataClient) {
  def track(session: UserSession, urn: Urn, secretToken: Option[String]): Future[Response] = {
    trackmetadataClient.track(session, urn, None).map {

      case Some(track) =>
        if (isAuthorized(session, secretToken, track)) {
          val res = Response()
          val singleTrackPublicApiRepresentation = new SingleTrackPublicApiRepresentation(
            "track",
            track.urn.getIdentifier.toLong,
            track.user_urn.getIdentifier.toLong)
          res.setContentString(Json.stringify(singleTrackPublicApiRepresentation))
          res
        } else {
          Response(Status.NotFound)
        }

      case None =>
        Response(Status.NotFound)
    }
  }

  private def isAuthorized(session: UserSession, secretToken: Option[String], track: Track): Boolean = {
    track.public ||
      track.user_urn == session.getUser ||
      (secretToken.filter(_ == track.secret_token).isDefined)
  }
}
