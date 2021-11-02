package com.soundcloud.publicApiStrangler.service.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps.JodaDateTimeExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.comments.{Comment => NewComment}
import com.soundcloud.publicApiStrangler.client.moshimoshicomments._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.mothership.{MoshimoshiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.handler.comments.CreateCommentParams
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import proto.soundcloud.comments.api.{CommentsClientProtobuf, GetCommentsResponse, Comment => ProtoComment}
import proto.soundcloud.tracks.api.{GetTrackCommentsResponse, CommentsClientProtobuf => TracksCommentsClientProtobuf}

import scala.util.Try

class CommentServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val okidokiClient = mock[RichOkidokiClient]
    val moshimoshiClient = mock[MoshimoshiClient]
    val moshimoshiCommentsClient = mock[MoshimoshiCommentsClient]
    val commentsTwirpClient = mock[CommentsClientProtobuf]
    val tracksTwirpClient = mock[TracksCommentsClientProtobuf]

    val rollout = mock[Rollout]

    val trackId = 4876
    val trackUrn = Urn("soundcloud", "tracks", trackId.toString())

    val moshimoshiResponse = Fixtures.okidokiComment

    val someClientId = "veryrealclientid"

    val pagination = OffsetBasedPagination(
      "http://api.example.com",
      s"/tracks/${trackId}/comments",
      ParamMap("filter_replies" -> "0", "threaded" -> "0", "client_id" -> someClientId),
      None,
      10
    )

    val firstCommentTime = "2017-08-23T15:25:14.154Z"
    val secondCommentTime = "2017-08-23T14:25:14.154Z"
    val thirdCommentTime = "2017-08-23T13:25:14.154Z"

    val firstCommentBody = "I am a comment. I represent opinion."
    val secondCommentBody = "Computers are useless. They can only give you answers."
    val thirdCommentBody = "Those are my principles. If you don’t like them I have others."

    private def createComment(id: Long, created_at: String, user_id: Long, timestamp: Option[Int], body: String) =
      MoshimoshiCommentsComment(
        self = MoshimoshiCommentsSelf(Urn("soundcloud", "comments", id.toString)),
        created_at = created_at,
        user = MoshimoshiCommentsCommentUser(MoshimoshiCommentsSelf(Urn("soundcloud", "users", user_id.toString()))),
        track = Urn("soundlcoud", "tracks", trackId.toString),
        timestamp = timestamp,
        body = body
      )

    def MoshimoshiCommentResponse(
        newComment: NewComment
    ) =
      Comment(
        newComment.urn.identifier.toLong,
        newComment.body,
        convertFormat(newComment.createdAt.toString),
        toInt(newComment.timestamp),
        newComment.track.identifier.toLong,
        newComment.user.identifier.toLong,
        UserBuilder.user(newComment.user.identifier.toInt)
      )

    // MoshimoshiCommentsComment -> Comment
    val comments = List(
      (
        createComment(1, firstCommentTime, 20, Some(1000), firstCommentBody),
        Comment(
          1,
          firstCommentBody,
          firstCommentTime,
          Some(1000),
          trackId,
          20,
          UserBuilder.user(20)
        )
      ),
      (
        createComment(2, secondCommentTime, 21, Some(1500), secondCommentBody),
        Comment(
          2,
          secondCommentBody,
          secondCommentTime,
          Some(1500),
          trackId,
          21,
          UserBuilder.user(21)
        )
      ),
      (
        createComment(3, thirdCommentTime, 22, Some(1600), thirdCommentBody),
        Comment(
          3,
          thirdCommentBody,
          thirdCommentTime,
          Some(1600),
          trackId,
          22,
          UserBuilder.user(22)
        )
      )
    )

    val comment1 =
      new NewComment(
        Urn("soundlcoud", "comments", "1"),
        Urn("soundlcoud", "tracks", "4876"),
        UserBuilder.urnFor(20),
        DateTime.parse(firstCommentTime),
        Some(1000),
        firstCommentBody
      )
    val comment2 =
      new NewComment(
        Urn("soundlcoud", "comments", "2"),
        Urn("soundlcoud", "tracks", "4876"),
        UserBuilder.urnFor(21),
        DateTime.parse(secondCommentTime),
        Some(1000),
        secondCommentBody
      )
    val comment3 =
      new NewComment(
        Urn("soundlcoud", "comments", "3"),
        Urn("soundlcoud", "tracks", "4876"),
        UserBuilder.urnFor(22),
        DateTime.parse(thirdCommentTime),
        Some(1000),
        thirdCommentBody
      )

    def toInt(o: Option[Long]): Option[Int] = o.flatMap(s => Try(s.toInt).toOption)
    val outputFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").withZoneUTC()
    val formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    def convertFormat(createdAt: String) = outputFormat.print(formatter.parseDateTime(createdAt))

    def toComment(newComment: NewComment): Comment = {
      Comment(
        newComment.urn.identifier.toLong,
        newComment.body,
        convertFormat(newComment.createdAt.toString),
        toInt(newComment.timestamp),
        newComment.track.identifier.toLong,
        newComment.user.identifier.toLong,
        UserBuilder.user(newComment.user.identifier.toInt)
      )
    }

    def toProtoComment(newComment: NewComment): ProtoComment = {
      ProtoComment(
        newComment.urn.toString,
        newComment.track.toString,
        newComment.user.toString,
        newComment.body,
        Some(JodaDateTimeExt(newComment.createdAt).asProto),
        toInt(newComment.timestamp)
      )
    }

    val newComments = Seq(comment1, comment2, comment3)
    var newCommentResponse = newComments.map(MoshimoshiCommentResponse(_))
    val response: Seq[Comment] = newComments.map(toComment)
    val commentUrns =
      Seq(Urn("soundcloud", "comments", "1"), Urn("soundcloud", "comments", "2"), Urn("soundcloud", "comments", "3"))
        .map(_.toString)

    val commentsvasTrackComments = BasicRolloutFeature(
      "tracks-vas-track-comments"
    )
    rollout.isActive(commentsvasTrackComments) returns Future.value(false)

    val commentService =
      new CommentService(
        okidokiClient,
        moshimoshiClient,
        moshimoshiCommentsClient,
        tracksTwirpClient,
        commentsTwirpClient,
        rollout
      )

  }

  "#fetchTrackComments" >> {

    "does not call comments VAS nor tracks VAS" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(NotValid("").bad))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      there was no(tracksTwirpClient).getTrackComments(any)
      there was no(commentsTwirpClient).getComments(any)
      result ==== NotValid("").bad
    }

    "returns a bad outcome if the track comments could not be fetched" in new Context {
      val expectedResponse = NotValid("").bad

      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(expectedResponse))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== expectedResponse
    }

    "forwards a not-found-result from the client" in new Context {
      val expectedResponse = NotFound().bad

      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(expectedResponse))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== expectedResponse
    }

    "fetches the user representations for each comment" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
        Future(
          MoshimoshiCommentsPagedResponse(comments.map(_._1), None).good
        )
      )

      when(
        okidokiClient.fetchUsersMap(
          anonymousSession,
          Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        )
      ).thenReturn(
        Future.value(
          Map(
            UserBuilder.urnFor(20) -> UserBuilder.user(20),
            UserBuilder.urnFor(21) -> UserBuilder.user(21),
            UserBuilder.urnFor(22) -> UserBuilder.user(22)
          )
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      verify(moshimoshiCommentsClient).fetchTrackComments(anonymousSession, trackUrn, pagination)

      result ==== Collection(comments.map(_._2), None).good
    }

    "filters out comments where the user could not be fetched" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
        Future(
          MoshimoshiCommentsPagedResponse(comments.map(_._1), None).good
        )
      )

      val users = Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
      when(okidokiClient.fetchUsersMap(anonymousSession, users)).thenReturn(
        Future.value(Map(UserBuilder.urnFor(21) -> UserBuilder.user(21)))
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== Collection(List(comments(1)._2), None).good
    }

    "builds the next href if the backing client returns a next_href" in new Context {
      when(moshimoshiCommentsClient.fetchTrackComments(any, any, any))
        .thenReturn(
          Future.value(MoshimoshiCommentsPagedResponse(Seq.empty, Some("http://the-next-href-of-your-dreams.com")).good)
        )

      when(okidokiClient.fetchUsersMap(anonymousSession, Set.empty))
        .thenReturn(Future(Map.empty[Urn, UserRepresentation]))

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== Collection(List[Comment](), Some(pagination.nextPage.normalizedHref)).good
    }
  }

  "#fetchTrackComments with new comments vas" >> {

    trait CommentsNewContext extends Context {
      rollout.isActive(commentsvasTrackComments) returns Future.value(true)
    }

    "does not call moshimoshi-comments" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.InvalidArgument, "")
        )
      )

      Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      there was no(moshimoshiCommentsClient).fetchTrackComments(any, any, any)
    }

    "returns NotValid if the request to tracks vas returns Invalid argument" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.InvalidArgument, "")
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== NotValid("").bad
    }

    "returns empty comments list if tracks vas returns PermissionDenied" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.PermissionDenied, "")
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== Collection(Seq[Comment]().toList, None).good
    }

    "returns empty comments list if tracks vas returns NotFound" in new CommentsNewContext {

      when(tracksTwirpClient.getTrackComments(any)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.NotFound, "")
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== Collection(Seq[Comment]().toList, None).good
    }

    "returns NotValid if comments vas returns InvalidArgument" in new CommentsNewContext {
      val expectedResponse = NotValid("").bad

      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.value(GetTrackCommentsResponse(commentUrns)))

      when(commentsTwirpClient.getComments(any)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.InvalidArgument, "")
        )
      )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== expectedResponse
    }

    "throws exception if the tracks vas is throwing a RuntimeException" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.exception(TwinagleException(ErrorCode.Internal, "internal server error")))

      Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value) must throwA(
        new RuntimeException(
          s"unexpected response from tracks: internal server error"
        )
      )
    }

    "throws exception if the comments vas is throwing a RuntimeException" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.value(GetTrackCommentsResponse(commentUrns)))

      when(commentsTwirpClient.getComments(any))
        .thenReturn(Future.exception(TwinagleException(ErrorCode.Internal, "internal server error")))

      Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value) must throwA(
        new RuntimeException(
          s"unexpected response from comments: internal server error"
        )
      )
    }

    "fetches the user representations for each comment" in new CommentsNewContext {
      val secretToken = Some("secret-token")

      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.value(GetTrackCommentsResponse(commentUrns)))

      when(commentsTwirpClient.getComments(any))
        .thenReturn(Future.value(GetCommentsResponse(newComments.map(toProtoComment))))

      when(
        okidokiClient.fetchUsersMap(
          anonymousSession,
          Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        )
      ).thenReturn(
        Future.value(
          Map(
            UserBuilder.urnFor(20) -> UserBuilder.user(20),
            UserBuilder.urnFor(21) -> UserBuilder.user(21),
            UserBuilder.urnFor(22) -> UserBuilder.user(22)
          )
        )
      )

      val result =
        Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination, secretToken).value)

      verify(commentsTwirpClient).getComments(any)
      verify(tracksTwirpClient).getTrackComments(any)
      verifyNoInteractions(moshimoshiCommentsClient)
      result ==== Collection(newCommentResponse.toList, None).good

    }

    "filters out comments where the user could not be fetched" in new CommentsNewContext {

      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.value(GetTrackCommentsResponse(commentUrns)))

      when(commentsTwirpClient.getComments(any))
        .thenReturn(Future.value(GetCommentsResponse(newComments.map(toProtoComment))))

      val users = Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
      when(okidokiClient.fetchUsersMap(anonymousSession, users)).thenReturn(
        Future.value(Map(UserBuilder.urnFor(21) -> UserBuilder.user(21)))
      )

      val result =
        Await.result(
          commentService.fetchTracksComments(anonymousSession, trackUrn, pagination, Some("secret-token")).value
        )

      verify(commentsTwirpClient).getComments(any)
      verify(tracksTwirpClient).getTrackComments(any)
      verifyNoInteractions(moshimoshiCommentsClient)
      result ==== Collection(Seq(comment2).map(MoshimoshiCommentResponse).toList, None).good

    }

    "builds the next href if the backing client returns a next_href" in new CommentsNewContext {
      when(tracksTwirpClient.getTrackComments(any))
        .thenReturn(Future.value(GetTrackCommentsResponse(commentUrns)))

      when(commentsTwirpClient.getComments(any))
        .thenReturn(Future.value(GetCommentsResponse(newComments.map(toProtoComment))))

      override val pagination = OffsetBasedPagination(
        "http://api.example.com",
        s"/tracks/${trackId}/comments",
        ParamMap("filter_replies" -> "0", "threaded" -> "0", "client_id" -> someClientId),
        None,
        3
      )

      when(
        okidokiClient.fetchUsersMap(
          anonymousSession,
          Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        )
      ).thenReturn(
        Future.value(
          Map(
            UserBuilder.urnFor(20) -> UserBuilder.user(20),
            UserBuilder.urnFor(21) -> UserBuilder.user(21),
            UserBuilder.urnFor(22) -> UserBuilder.user(22)
          )
        )
      )

      val result =
        Await.result(
          commentService.fetchTracksComments(anonymousSession, trackUrn, pagination, Some("secret-token")).value
        )

      result ==== Collection(newCommentResponse.toList, Some(pagination.nextPage.normalizedHref)).good
      verify(commentsTwirpClient).getComments(any)
      verify(tracksTwirpClient).getTrackComments(any)
      verifyNoInteractions(moshimoshiCommentsClient)

    }
  }

  "#createComment" >> {
    "returns Comment on success" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]
      val user = UserBuilder.user(1)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map(okidokiComment.user.self.urn -> user)))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== Comment.fromOkidokiComment(okidokiComment, user).good
    }

    "adds secret token to comment if present" in new Context {
      val secretToken = Some("secret-token")
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), secretToken)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]
      val user = UserBuilder.user(1)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map(okidokiComment.user.self.urn -> user)))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result match {
        case Good(comment) => comment.uri ==== "https://api.soundcloud.com/comments/123?secret_token=secret-token"
        case Bad(_) => true ==== false
      }
    }

    "forwards failures" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams)).thenReturn(Future(NotValid("").bad))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== NotValid("").bad
    }

    "handles userservice failures" in new Context {
      val createCommentParams = CreateCommentParams(trackUrn, "bar", Some(1000), None)
      val okidokiComment = moshimoshiResponse.as[MoshimoshiCommentsComment]

      when(moshimoshiClient.createComment(anonymousSession, createCommentParams))
        .thenReturn(Future(okidokiComment.good))
      when(okidokiClient.fetchUsersMap(anonymousSession, Set(okidokiComment.user.self.urn)))
        .thenReturn(Future(Map.empty[Urn, UserRepresentation]))

      val result = Await.result(commentService.createComment(anonymousSession, createCommentParams))
      result ==== NotFound().bad
    }
  }
}
