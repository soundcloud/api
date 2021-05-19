package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.{
  Created,
  Deleted,
  Failed,
  Forbidden,
  NotFound,
  Result
}
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
    trackRepostsService: TrackRepostsService
) {

  def createTracksRepost(session: UserSession, trackUrn: Urn): Future[Result] = {
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)

    trackRepostsService
      .repostTrack(request)
      .map(_ => Created)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound
        case TwinagleException(ErrorCode.PermissionDenied, _, _, _) => Forbidden
        case TwinagleException(_, _, _, _) => Failed
      }
  }

  def deleteTracksRepost(session: UserSession, trackUrn: Urn): Future[Result] = {
    val request = RepostTrackRequest(Some(session.asProtoSession), trackUrn.toString)

    trackRepostsService
      .deleteTrackRepost(request)
      .map(_ => Deleted)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) => NotFound
        case TwinagleException(_, _, _, _) => Failed
      }
  }

  def createPlaylistsRepost(session: UserSession, playlist: Urn): Future[Result] = {
    repostsClient.createRepost(session, playlist)
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

}
