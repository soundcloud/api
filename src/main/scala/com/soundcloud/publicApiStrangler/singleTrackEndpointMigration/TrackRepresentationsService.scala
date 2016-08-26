package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.OkidokiClient
import com.soundcloud.service.response.representation.User
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal}

class TrackRepresentationsService(
  trackmetadataClient: TrackmetadataClient,
  okidokiClient: OkidokiClient,
  pubmeseClient: PubmeseClient,
  stitchClient: StitchClient) {

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
  private val serviceUnavailableErrorString = """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""

  def track(session: UserSession, urn: Urn, secretToken: Option[String], callback: Option[String]): Future[Response] = {
    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case ex: Exception => None }
    trackmetadataClient.track(session, urn).flatMap {
      case Some(track) if isTrackAccessible(session, secretToken, track) =>
        val userF = fetchUserForTrack(track, session)
        val countsF = userF.flatMap {
          case Some(user) => stitchClient.countsForTrack(session, urn, user.urn).map(Some(_))
          case None => Future.value(None)
        }
        Future.join(isrcF, userF, countsF).map {
          case (isrc, Some(user), Some(counts)) =>
            val content = jsonForTrack(track, user, isrc, counts)
            generateResponse(Status.Ok, jsonpWrapper(callback, content))
          case _ =>
            generateResponse(Status.NotFound, jsonpWrapper(callback, notFoundErrorString))
        }
        .handle {
          case NonFatal(ex) =>
            generateResponse(Status.ServiceUnavailable, jsonpWrapper(callback, serviceUnavailableErrorString))
        }
      case _ =>
        Future.value(generateResponse(Status.NotFound, jsonpWrapper(callback, notFoundErrorString)))
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

  private def jsonForTrack(track: Track, user: User, isrc: Option[Isrc], counts: StitchCounts) =
    Json.stringify(new TrackRepresentation(track, user, isrc, counts))

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
