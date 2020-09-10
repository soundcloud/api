package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.liebling._
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.playlists.representation.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.util.Future

sealed trait CreateResponse
case object OkCreateResponse extends CreateResponse
case object OkCreatedCreateResponse extends CreateResponse
case object NotAuthorizedCreateResponse extends CreateResponse
case object NotFoundCreateResponse extends CreateResponse
case object SpamBlockedCreateResponse extends CreateResponse

class LikesService(
    trackRepresentationsService: TrackRepresentationsService,
    lieblingClient: LieblingClient
) {

  def createTrackLike(
      session: UserSession,
      urn: Urn
  ) = {
    lieblingClient.createTrackLike(session, urn).map {
      case LikeCreated => OkCreatedCreateResponse
      case LikeAlreadyExists => OkCreateResponse
      case UserBlocked => NotAuthorizedCreateResponse
      case UserHasSpamWarning => SpamBlockedCreateResponse
      case _ => NotFoundCreateResponse
    }
  }

  def deleteTrackLike(session: UserSession, urn: Urn): Future[DeleteLikeResponse] =
    lieblingClient.deleteTrackLike(session, urn)

  def userTrackLikeForUrn(
      session: UserSession,
      userUrn: Urn,
      trackUrn: Urn
  ): Future[Option[TrackRepresentation]] = {
    for {
      likedTrackUrns <- lieblingClient.userTracksLikesForUrns(session, userUrn, List(trackUrn))
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likedTrackUrns.map(track => TrackRequest(track, None))
      )
    } yield {
      enrichedTracks.headOption
    }
  }

  def userTracksLikes(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[TrackRepresentation]] = {
    for {
      likesPage <- lieblingClient.userTracksLikes(session, userUrn, pagination.cursor, pagination.pageSize)
      enrichedTracks <- trackRepresentationsService.tracks(
        session,
        likesPage.likes.map(like => TrackRequest(like.target_urn, None))
      )
    } yield {
      val nextHref =
        likesPage.meta.cursor.next_params.map(params => pagination.nextPage(params.cursor)).map(_.normalizedHref)

      Collection(enrichedTracks, nextHref)
    }
  }
}
