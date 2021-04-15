package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{LikeTrackRequest, LikesClientProtobuf}
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams

sealed trait CreateResponse
case object OkCreateResponse extends CreateResponse
case object OkCreatedCreateResponse extends CreateResponse
case object NotAuthorizedCreateResponse extends CreateResponse
case object NotFoundCreateResponse extends CreateResponse
case object SpamBlockedCreateResponse extends CreateResponse

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    lieblingClient: LieblingClient,
    likesTwirpClient: LikesClientProtobuf
) {

  def createTrackLike(
      session: UserSession,
      urn: Urn
  ): Future[CreateResponse] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    likesTwirpClient
      .likeTrack(request)
      .map(_ => OkCreateResponse)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFoundCreateResponse
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => NotAuthorizedCreateResponse
        case TwinagleException(ErrorCode.ResourceExhausted, _, _, _) => SpamBlockedCreateResponse
        case TwinagleException(_, msg, _, _) => throw new RuntimeException(s"unexpected response from tracks: ${msg}")
      }
  }

  def deleteTrackLike(session: UserSession, urn: Urn): Future[DeleteLikeResponse] = {
    val request = LikeTrackRequest(userSession = Some(session.asProtoSession), trackUrn = urn.toString)

    likesTwirpClient
      .unlikeTrack(request)
      .map(_ => LikeDeleted)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => LikeNotFound
        case TwinagleException(_, msg, _, _) => throw new RuntimeException(s"unexpected response from tracks: ${msg}")
      }
  }

  def createPlaylistLike(
      session: UserSession,
      urn: Urn
  ) = {
    lieblingClient.createPlaylistLike(session, urn).map {
      case LikeCreated => OkCreatedCreateResponse
      case LikeAlreadyExists => OkCreateResponse
      case UserBlocked => NotAuthorizedCreateResponse
      case UserHasSpamWarning => SpamBlockedCreateResponse
      case _ => NotFoundCreateResponse
    }
  }

  def deletePlaylistLike(session: UserSession, urn: Urn): Future[DeleteLikeResponse] =
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
