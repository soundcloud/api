package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Track, TrackmetadataClient}
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.{Urn, UserSession}
import com.soundcloud.service.client.{MoshimoshiClient, OkidokiClient}
import com.soundcloud.service.response.representation.User
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, NonFatal}

class TrackRepresentationsService(
  trackmetadataClient: TrackmetadataClient,
  okidokiClient: OkidokiClient,
  pubmeseClient: PubmeseClient,
  stitchClient: StitchClient,
  moshimoshiClient: MoshimoshiClient) {

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
  private val serviceUnavailableErrorString = """{"errors":[{"error_message":"503 - Service Unavailable"}]}"""

  def track(session: UserSession, urn: Urn, secretToken: Option[String], callback: Option[String]): Future[Response] = {
    val isrcF = pubmeseClient.isrcForTrack(session, urn).handle { case NonFatal(ex) => None }
    val geoblockingsF = moshimoshiClient.fetchTrackGeoblockings(session, urn).handle { case NonFatal(ex) => None }

    trackmetadataClient.track(session, urn).flatMap {
      case Some(track) if isTrackAccessible(session, secretToken, track) =>
        val userF = fetchUserForTrack(track, session)
        val labelF = fetchLabelForTrack(track, session)
        val countsF = userF.flatMap {
          case Some(user) => stitchClient.countsForTrack(session, urn, user.urn).map(Some(_)).liftToTry.map(_.getOrElse(None))
          case None => Future.value(None)
        }
        Future.join(isrcF, userF, countsF, labelF, geoblockingsF).map {
          case (isrc, Some(user), counts, label, geoblockings) =>
            val rep = TrackRepresentation(
              track = track,
              user = user,
              isrc = isrc,
              counts = getCounts(counts),
              label = label,
              geoblockings = geoblockings
            )
            generateResponse(Status.Ok, jsonpWrapper(callback, Json.stringify(rep)))
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
    fetchUser(track.user_urn, session)

  private def fetchLabelForTrack(track: Track, session: UserSession): Future[Option[User]] = track.label_id match {
    case Some(label_id) => fetchUser(new Urn("soundcloud", "users", label_id.toString), session)
    case None => Future.value(None)
  }

  private def fetchUser(userUrn: Urn, session: UserSession): Future[Option[User]] =
    okidokiClient.fetchUserObjects(session, Set(userUrn)).map(_.headOption)

  private def getCounts(counts: Option[StitchCounts]): StitchCounts =
    counts.getOrElse(StitchCounts(0, 0, 0, 0))

  private def isTrackAccessible(session: UserSession, secretToken: Option[String], track: Track): Boolean =
    isPrivacyAuthorized(session, secretToken, track) && !isDisabled(track)

  private def isPrivacyAuthorized(session: UserSession, secretToken: Option[String], track: Track): Boolean =
    track.public || track.user_urn == session.getUser || secretToken.contains(track.secret_token)

  private def isDisabled(track: Track): Boolean =
    track.disabled_at.isDefined
}
