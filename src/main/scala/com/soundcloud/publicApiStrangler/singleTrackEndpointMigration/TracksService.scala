package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TracksService(
                     trackmetadataClient: TrackmetadataClient,
                     contentAuthorization: ContentAuthorizationRules
                   ) {
  def track(session: UserSession, urn: Urn, secretToken: Option[String]): Future[Response] = {
    trackmetadataClient.track(session, urn).flatMap {

      case Some(track) =>
        if (isPrivacyAuthorized(session, secretToken, track)) {
          isContentAuthorized(session, urn).map {
            case true =>
              val res = Response()
              val singleTrackPublicApiRepresentation = new SingleTrackPublicApiRepresentation(
                "track",
                track.urn.getIdentifier.toLong,
                track.user_urn.getIdentifier.toLong)
              res.setContentString(Json.stringify(singleTrackPublicApiRepresentation))
              res
            case false =>
              Response(Status.NotFound)
          }
        } else {
          Future.value(Response(Status.NotFound))
        }

      case None =>
        Future.value(Response(Status.NotFound))
    }
  }

  private def isPrivacyAuthorized(session: UserSession, secretToken: Option[String], track: Track): Boolean = {
    track.public ||
      track.user_urn == session.getUser ||
      (secretToken.filter(_ == track.secret_token).isDefined)
  }

  private def isContentAuthorized(session: UserSession, urn: Urn): Future[Boolean] = {
    contentAuthorization.fetchRules(session, Seq(urn)).map {
      case rules =>
        rules.filter(_.getUrn == urn).filter(_.getPolicy == ContentPolicy.BLOCK).isEmpty
    }
  }
}
