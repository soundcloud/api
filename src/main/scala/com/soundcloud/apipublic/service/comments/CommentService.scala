package com.soundcloud.apipublic.service.comments

import com.soundcloud.apipublic.client.comments.{CommentsClient, Comment => CommentFromVAS}
import com.soundcloud.apipublic.client.moshimoshicomments.{
  Comment,
  MoshimoshiCommentsClient,
  MoshimoshiCommentsComment,
  MoshimoshiCommentsPagedResponse
}
import com.soundcloud.apipublic.client.mothership.{MoshimoshiClient, RichOkidokiClient}
import com.soundcloud.apipublic.client.tracks.TracksClient
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.DateTimeFormat

class CommentService(
    okidokiClient: RichOkidokiClient,
    moshimoshiClient: MoshimoshiClient,
    moshimoshiCommentsClient: MoshimoshiCommentsClient,
    tracksClient: TracksClient,
    commentClient: CommentsClient,
    rolloutClient: Rollout
) {

  private val tracksVasTrackComments = BasicRolloutFeature(
    "tracks-vas-track-comments"
  )

  def fetchTracksComments(
      session: UserSession,
      track: Urn,
      pagination: OffsetBasedPagination,
      secretToken: Option[String] = None
  ): OutcomeF[Collection[Comment]] = {
    for {
      shouldFetchFromVas <- rolloutClient.isActive(tracksVasTrackComments).outcomeF
      comments <- shouldFetchFromVas match {
        case false => fetchFromMoshimoshi(session, track, pagination)
        case true => fetchFromComments(session, track, secretToken, pagination)
      }
    } yield comments
  }

  private def fetchFromMoshimoshi(
      session: UserSession,
      track: Urn,
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[Comment]] = {
    for {
      moshiComments <- moshimoshiCommentsClient.fetchTrackComments(session, track, pagination).outcomeF
      materializedComments <- buildCommentsFromMoshimoshiResponse(session, moshiComments, pagination)
    } yield materializedComments
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

  private val tracksVasCreateComment = BasicRolloutFeature(
    "tracks-vas-create-comment"
  )

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
    for {
      shouldUseCommentsVas <- rolloutClient.isActive(tracksVasCreateComment).outcomeF
      response <- shouldUseCommentsVas match {
        case false => createMoshimoshiComment(session, params)
        case true => createTrackComment(session, params, createdAtVasValueOverride)
      }
    } yield response

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

  private def createMoshimoshiComment(
      session: UserSession,
      params: CreateCommentParams
  ): OutcomeF[Comment] = {
    moshimoshiClient
      .createComment(session, params)
      .flatMap {
        case Good(okidokiComment) =>
          okidokiClient
            .fetchUsersMap(session, Set(okidokiComment.user.self.urn))
            .map(usersMap =>
              usersMap.get(okidokiComment.user.self.urn) match {
                case Some(miniUser) => Comment.fromOkidokiComment(okidokiComment, miniUser, params.secretToken).good
                case None => NotFound().bad
              }
            )
        case Bad(badThing) => Future.value(badThing.bad)
      }
      .outcomeF
  }

  private def buildCommentsFromMoshimoshiResponse(
      session: UserSession,
      moshimoshiComments: MoshimoshiCommentsPagedResponse,
      pagination: OffsetBasedPagination
  ): OutcomeF[Collection[Comment]] = {
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
      .outcomeF
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
              id = comment.urn.identifier.toLong,
              body = comment.body,
              createdAt = comment.createdAt.map(format).getOrElse(""),
              trackId = comment.track.identifier.toLong,
              userId = comment.user.identifier.toLong,
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
