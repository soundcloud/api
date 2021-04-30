package com.soundcloud.publicApiStrangler.service.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.comments.{
  Comment,
  MoshimoshiCommentsClient,
  MoshimoshiCommentsComment,
  MoshimoshiCommentsPagedResponse
}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.mothership.{MoshimoshiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.handler.comments.CreateCommentParams
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.util.Future

class CommentService(
    okidokiClient: RichOkidokiClient,
    moshimoshiClient: MoshimoshiClient,
    moshimoshiCommentsClient: MoshimoshiCommentsClient
) {

  def fetchTracksComments(
      session: UserSession,
      track: Urn,
      pagination: OffsetBasedPagination
  ): Future[Outcome[Collection[Comment]]] = {
    moshimoshiCommentsClient
      .fetchTrackComments(session, track, pagination)
      .flatMap {
        case Good(moshimoshiComments) => buildComments(session, moshimoshiComments, pagination)
        case Bad(outcome) => Future.value(outcome.bad)
      }
  }

  def createComment(session: UserSession, params: CreateCommentParams): Future[Outcome[Comment]] = {
    moshimoshiClient
      .createComment(session, params)
      .flatMap {
        case Good(okidokiComment) => {
          fetchUsers(session, okidokiComment).map(usersMap =>
            usersMap.get(okidokiComment.user.self.urn) match {
              case Some(miniUser) => Comment.fromOkidokiComment(okidokiComment, miniUser, params.secretToken).good
              case None => NotFound().bad
            }
          )
        }
        case Bad(badThing) => Future.value(badThing.bad)
      }
  }

  private def fetchUsers(
      session: UserSession,
      moshimoshiComment: MoshimoshiCommentsComment
  ): Future[Map[Urn, UserRepresentation]] = {
    okidokiClient.fetchUsersMap(session, Set(moshimoshiComment.user.self.urn))
  }

  private def buildComments(
      session: UserSession,
      moshimoshiComments: MoshimoshiCommentsPagedResponse,
      pagination: OffsetBasedPagination
  ): Future[Outcome[Collection[Comment]]] = {
    okidokiClient
      .fetchUsersMap(session, moshimoshiComments.collection.map(_.user.self.urn).toSet)
      .map { urnToUserMap =>
        val comments = moshimoshiComments.collection
          .collect({
            case moshiComment: MoshimoshiCommentsComment if urnToUserMap.contains(moshiComment.user.self.urn) =>
              Comment(
                id = moshiComment.self.urn.identifier.toLong,
                body = moshiComment.body,
                createdAt = moshiComment.created_at,
                timestamp = moshiComment.timestamp,
                trackId = moshiComment.track.identifier.toLong,
                userId = moshiComment.user.self.urn.identifier.toLong,
                user = urnToUserMap(moshiComment.user.self.urn)
              )
          })
        val nextHref = moshimoshiComments.next_href.map(_ => pagination.nextPage.normalizedHref)
        Collection(comments.toList, nextHref).good
      }
  }
}
