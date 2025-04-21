package com.soundcloud.apipublic.service.comments

import com.soundcloud.apipublic.client.comments.{Comment, CommentFromVAS, CommentsClient}
import com.soundcloud.apipublic.client.mothership.RichOkidokiClient
import com.soundcloud.apipublic.client.tracks.TracksClient
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

class CommentService(
    okidokiClient: RichOkidokiClient,
    tracksClient: TracksClient,
    commentClient: CommentsClient
) {

  def fetchTracksComments(
      session: UserSession,
      track: Urn,
      pagination: OffsetBasedPagination,
      secretToken: Option[String] = None
  ): OutcomeF[Collection[Comment]] = {
    fetchFromComments(session, track, secretToken, pagination)
  }

  private def fetchFromComments(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[Comment]] = {

    for {
      commentUrns <- tracksClient.getComments(
        session,
        trackUrn,
        secretToken,
        pagination.offset.getOrElse(0),
        pagination.limit
      )
      comments <- commentClient.getComments(commentUrns)
      materializedComments <- buildComments(session, comments, pagination)
    } yield materializedComments
  }

  /**
    * Creates track comments
    *
    * @param session User session
    * @param params Comment params
    * @param createdAtVasValueOverride This parameter specifies a createdAt field value of a newly created
    *                                  comment on VAS. This value is never passed to the downstream backends.
    *                                  It's needed on a new comment creation since requesting a freshly created comment
    *                                  from the comments VAS is unstable due to replication lag, so BFF will build the
    *                                  comment from params.
    * @return
    */
  def createComment(
      session: UserSession,
      params: CreateCommentParams,
      createdAtVasValueOverride: DateTime = DateTime.now(DateTimeZone.UTC)
  ): OutcomeF[Comment] = {
    createTrackComment(session, params, createdAtVasValueOverride)
  }

  private def createTrackComment(
      session: UserSession,
      params: CreateCommentParams,
      createdAtVasValueOverride: DateTime
  ): OutcomeF[Comment] = {

    for {
      user <- okidokiClient
        .fetchUsersMap(session, Set(session.getUser))
        .map(usersMap =>
          usersMap.get(session.getUser) match {
            case Some(miniUser) => miniUser.good
            case None => NotFound().bad
          }
        )
        .outcomeF
      commentUrn <- tracksClient
        .createComment(session, params)
    } yield Comment.fromVASComment(commentUrn, params, createdAtVasValueOverride, user)
  }

  private def buildComments(
      session: UserSession,
      comments: Seq[CommentFromVAS],
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[Comment]] = {

    if (comments.isEmpty) {
      return Collection(List[Comment](), None).goodF
    }

    okidokiClient
      .fetchUsersMap(session, comments.map(_.user).toSet)
      .map { urnToUserMap =>
        val commentsResponse: Seq[Comment] = comments.collect({
          case comment: CommentFromVAS if urnToUserMap.contains(comment.user) =>
            Comment(
              urn = comment.urn,
              body = comment.body,
              createdAt = comment.createdAt.map(format).getOrElse(""),
              trackUrn = comment.track,
              user = urnToUserMap(comment.user),
              timestamp = CommentFromVAS.toInt(comment.timestamp)
            )
        })
        val nextHref =
          if (commentsResponse.size == pagination.limit) {
            Some(pagination.nextPage.normalizedHref)
          } else
            None
        Collection(commentsResponse.toList, nextHref).good
      }
      .outcomeF
  }

  private def format(dateTime: DateTime): String = {
    DateTimeFormat
      .forPattern("yyyy/MM/dd HH:mm:ss Z")
      .print(dateTime)
  }
}
