package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.User
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TracksService(trackmetadataClient: TrackmetadataClient, okidokiClient: OkidokiClient) {

  private val errorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  def track(session: UserSession, urn: Urn, secretToken: Option[String], callback: Option[String]): Future[Response] = {
    val content =
      trackmetadataClient.track(session, urn).flatMap {
        case Some(track) if isTrackAccessible(session, secretToken, track) =>
          fetchUserForTrack(track, session).map {
            case Some(user) => Some(jsonForTrack(track, user))
            case _ => None
          }
        case _ => Future.value(None)
      }

    content map {
      case Some(content) =>
        generateResponse(Status.Ok, jsonpWrapper(callback, content))
      case _ =>
        generateResponse(Status.NotFound, jsonpWrapper(callback, errorString))
    }
  }

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private def generateResponse(status: Status, content: String): Response = {
    val contentLength = content.getBytes("UTF-8").length
    val res = Response(status)
    res.setContentString(content)
    res.contentType = "application/json; charset=utf-8"
    res.contentLength = contentLength
    res
  }

  private def fetchUserForTrack(track: Track, session: UserSession): Future[Option[User]] =
    okidokiClient.fetchUserObjects(session, Set(track.user_urn)).map(_.headOption)

  private def jsonForTrack(track: Track, user: User) =
    Json.stringify(new TrackRepresentation(track, user))

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
