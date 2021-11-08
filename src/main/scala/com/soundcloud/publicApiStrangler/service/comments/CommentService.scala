package com.soundcloud.publicApiStrangler.service.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.comments.{Comment => CommentFromVAS}
import com.soundcloud.publicApiStrangler.client.moshimoshicomments.{
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
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import proto.soundcloud.comments.api.{CommentsClientProtobuf, GetCommentsRequest}
import proto.soundcloud.tracks.api.{
  GetTrackCommentsRequest,
  GetTrackCommentsResponse,
  CommentsClientProtobuf => TracksCommentsClientProtobuf
}

class CommentService(
    okidokiClient: RichOkidokiClient,
    moshimoshiClient: MoshimoshiClient,
    moshimoshiCommentsClient: MoshimoshiCommentsClient,
    trackCommentsTwirpClient: TracksCommentsClientProtobuf,
    commentsTwirpClient: CommentsClientProtobuf,
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
      tracksResponse <- fetchUrnsFromTracks(
        session,
        trackUrn,
        secretToken,
        pagination.offset.getOrElse(0),
        pagination.limit
      )
      comments <- fetchFromComments(tracksResponse.commentsUrns)
      materializedComments <- buildComments(session, comments, pagination)
    } yield materializedComments
  }

  private def fetchUrnsFromTracks(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      offset: Int,
      limit: Int
  ): OutcomeF[GetTrackCommentsResponse] = {

    val request = GetTrackCommentsRequest(
      userSession = Some(session.asProtoSession),
      urn = trackUrn.toString,
      secretToken = secretToken,
      offset = offset,
      limit = limit
    )
    trackCommentsTwirpClient
      .getTrackComments(request)
      .map(response => GetTrackCommentsResponse(response.commentsUrns).good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) | TwinagleException(ErrorCode.PermissionDenied, _, _, _) =>
          GetTrackCommentsResponse(Seq.empty).good
        case TwinagleException(ErrorCode.InvalidArgument, reason, _, _) => NotValid(reason).bad
        case TwinagleException(_, msg, _, _) => throw new RuntimeException(s"unexpected response from tracks: $msg")
      }
      .outcomeF
  }

  private def fetchFromComments(
      urns: Seq[String]
  ): OutcomeF[Seq[CommentFromVAS]] = {

    if (urns.isEmpty) Seq.empty.goodF
    else {
      val request = GetCommentsRequest(
        urns = urns
      )
      commentsTwirpClient
        .getComments(request)
        .map(response => response.comments.map(CommentFromVAS.fromProto).good)
        .handle {
          case TwinagleException(ErrorCode.InvalidArgument, reason, _, _) => NotValid(reason).bad
          case TwinagleException(_, msg, _, _) =>
            throw new RuntimeException(s"unexpected response from comments: $msg")
        }
        .outcomeF
    }
  }

  def createComment(session: UserSession, params: CreateCommentParams): Future[Outcome[Comment]] = {
    moshimoshiClient
      .createComment(session, params)
      .flatMap {
        case Good(okidokiComment) =>
          fetchUsers(session, okidokiComment).map(usersMap =>
            usersMap.get(okidokiComment.user.self.urn) match {
              case Some(miniUser) => Comment.fromOkidokiComment(okidokiComment, miniUser, params.secretToken).good
              case None => NotFound().bad
            }
          )
        case Bad(badThing) => Future.value(badThing.bad)
      }
  }

  private def fetchUsers(
      session: UserSession,
      moshimoshiComment: MoshimoshiCommentsComment
  ): Future[Map[Urn, UserRepresentation]] = {
    okidokiClient.fetchUsersMap(session, Set(moshimoshiComment.user.self.urn))
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
