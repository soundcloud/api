package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.outcome.{ApplicationError, GoodOps, Outcome}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.twinagle.TwinagleException
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{LikeTrackRequest, LikesClientProtobuf}

case class CreateLikeResponse()
case class DeleteLikeResponse()

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    lieblingClient: LieblingClient,
    likesTwirpClient: LikesClientProtobuf
) {

  def createTrackLike(
      session: UserSession,
      urn: Urn
  ): Future[Outcome[CreateLikeResponse]] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    likesTwirpClient
      .likeTrack(request)
      .map(_ => CreateLikeResponse().good)
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

  def deleteTrackLike(session: UserSession, urn: Urn): Future[Outcome[DeleteLikeResponse]] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    likesTwirpClient
      .unlikeTrack(request)
      .map(_ => DeleteLikeResponse().good)
      .handle {
        case e @ TwinagleException(_, _, _, _) => ApplicationError.fromTwinagleException(e).bad
      }
  }

  def createPlaylistLike(
      session: UserSession,
      urn: Urn
  ): Future[Outcome[CreateLikeResponse]] =
    lieblingClient.createPlaylistLike(session, urn)

  def deletePlaylistLike(session: UserSession, urn: Urn): Future[Outcome[DeleteLikeResponse]] =
    lieblingClient.deletePlaylistLike(session, urn)

  def userTrackLikeForUrn(
      session: UserSession,
      userUrn: Urn,
      trackUrn: Urn
  ): Future[Option[TrackRepresentation]] = {
    for {
      likedTrackUrns <- lieblingClient.userTracksLikesForUrns(session, userUrn, List(trackUrn))
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likedTrackUrns.map(track => TrackRequest(track, None)),
        AccessParams.defaultAccess
      )
    } yield {
      enrichedTracks.headOption
    }
  }

  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      access: AccessParams,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    for {
      likesPage <- lieblingClient.userTracksLikes(session, userUrn, pagination.cursor, pagination.pageSize)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likesPage.likes.map(like => TrackRequest(like.target_urn, None)),
        access
      )
    } yield {
      val nextHref =
        likesPage.meta.cursor.next_params.map(params => pagination.nextPage(params.cursor)).map(_.normalizedHref)

      Collection(enrichedTracks, nextHref)
    }
  }
}
