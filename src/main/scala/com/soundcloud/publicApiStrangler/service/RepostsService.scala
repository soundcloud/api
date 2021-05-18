package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.{Deleted, Failed, NotFound, Result}
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserOrderingUtils.sortByProvidedUrns
import com.soundcloud.publicApiStrangler.service.users.UserRepresentationsService
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{RepostTrackRequest, RepostsService => TrackRepostsService}

class RepostsService(
    userRepresentationService: UserRepresentationsService,
    repostsClient: RepostsClient,
    trackRepostsService: TrackRepostsService,
    rollout: Rollout
) {
  val deleteTrackRepostTwirpRollout = RolloutFeature("delete-twirp-repost-tracks")

  def createRepost(session: UserSession, target: Urn): Future[Result] = {
    repostsClient.createRepost(session, target)
  }

  def deleteTracksRepost(session: UserSession, track: Urn): Future[Result] = {
    rollout
      .isActive(deleteTrackRepostTwirpRollout)
      .flatMap(isActive => {
        if (isActive) {
          deleteTrackRepost(session, track)
        } else {
          repostsClient.deleteRepost(session, track)
        }
      })
  }

  def deletePlaylistsRepost(session: UserSession, playlist: Urn): Future[Result] = {
    repostsClient.deleteRepost(session, playlist)
  }

  def getReposters(
      session: UserSession,
      target: Urn,
      pagination: CursorBasedPagination
  ): Future[Collection[UserRepresentation]] = {
    repostsClient
      .reposters(session, target, pagination.pageSize, pagination.cursor)
      .flatMap { reposts =>
        userRepresentationService
          .getUsers(session, reposts.urns.toSet)
          .map { users =>
            val nextHref = reposts.nextCursor.map(cursor => pagination.nextPage(cursor)).map(_.normalizedHref)
            Collection(sortByProvidedUrns(users, reposts.urns).toList, nextHref)
          }
      }
  }

  private def deleteTrackRepost(session: UserSession, trackUrn: Urn): Future[Result] = {
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)

    trackRepostsService
      .deleteTrackRepost(request)
      .map(_ => Deleted)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound
        case TwinagleException(_, _, _, _) => Failed
      }
  }

}
