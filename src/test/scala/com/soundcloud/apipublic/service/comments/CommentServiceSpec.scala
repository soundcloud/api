package com.soundcloud.apipublic.service.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps.JodaDateTimeExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.comments.{Comment => CommentFromVAS}
import com.soundcloud.apipublic.client.moshimoshicomments._
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.mothership.{MoshimoshiClient, RichOkidokiClient}
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.mockito.Mockito.{verify, when}
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

    val commentFromVAS1 =
      ProtoComment(
        "soundcloud:comments:1",
        "soundcloud:tracks:4876",
        "soundcloud:users:20",
        "I am a comment. I represent opinion.",
        Some(JodaDateTimeExt(DateTime.parse("2017-08-23T15:25:14.154Z")).asProto),
        Some(1000)
      )

    val commentFromVAS2 =
      ProtoComment(
        "soundcloud:comments:2",
        "soundcloud:tracks:4876",
        "soundcloud:users:21",
        "Computers are useless. They can only give you answers.",
        Some(JodaDateTimeExt(DateTime.parse("2017-08-23T14:25:14.154Z")).asProto),
        Some(1500)
      )

    val commentFromVAS3 =
      ProtoComment(
        "soundcloud:comments:3",
        "soundcloud:tracks:4876",
        "soundcloud:users:22",
        "Those are my principles. If you don’t like them I have others.",
        Some(JodaDateTimeExt(DateTime.parse("2017-08-23T13:25:14.154Z")).asProto),
        Some(1600)
      )

    val commentsFromVAS = Seq(commentFromVAS1, commentFromVAS2, commentFromVAS3)

    val moshimoshiCommentsResponse: Seq[MoshimoshiCommentsComment] = commentsFromVAS
      .map(CommentFromVAS.fromProto)
      .map(toMoshimoshiComment)

    private def toMoshimoshiComment(commentFromVAS: CommentFromVAS) =
      MoshimoshiCommentsComment(
        self = MoshimoshiCommentsSelf(commentFromVAS.urn),
        created_at = commentFromVAS.createdAt.map(format).getOrElse(""),
        user = MoshimoshiCommentsCommentUser(MoshimoshiCommentsSelf(commentFromVAS.user)),
        track = commentFromVAS.track,
        timestamp = toInt(commentFromVAS.timestamp),
        body = commentFromVAS.body
      )

    private def format(dateTime: DateTime): String = {
      DateTimeFormat
        .forPattern("yyyy/MM/dd HH:mm:ss Z")
        .print(dateTime)
    }

    var commentsInResponse: Seq[Comment] = commentsFromVAS
      .map(CommentFromVAS.fromProto)
      .map(toResponseRepresentation)

    def toResponseRepresentation(commentFromVAS: CommentFromVAS): Comment =
      Comment(
        commentFromVAS.urn.identifier.toLong,
        commentFromVAS.body,
        commentFromVAS.createdAt.map(format).getOrElse(""),
        toInt(commentFromVAS.timestamp),
        commentFromVAS.track.identifier.toLong,
        commentFromVAS.user.identifier.toLong,
        UserBuilder.user(commentFromVAS.user.identifier.toInt)
      )

    def toInt(o: Option[Long]): Option[Int] = o.flatMap(s => Try(s.toInt).toOption)

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
    "When the rollout is disabled" >> {
      "only calls moshimoshi-comments" in new Context {
        when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(Future.value(NotValid("").bad))

        val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

        there was no(tracksTwirpClient).getTrackComments(any)
        there was no(commentsTwirpClient).getComments(any)
        there was one(moshimoshiCommentsClient).fetchTrackComments(any, any, any)
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
            MoshimoshiCommentsPagedResponse(moshimoshiCommentsResponse, None).good
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

        result ==== Collection(commentsInResponse.toList, None).good
      }

      "filters out comments where the user could not be fetched" in new Context {
        when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
          Future(
            MoshimoshiCommentsPagedResponse(moshimoshiCommentsResponse, None).good
          )
        )

        val users = Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        when(okidokiClient.fetchUsersMap(anonymousSession, users)).thenReturn(
          Future.value(Map(UserBuilder.urnFor(21) -> UserBuilder.user(21)))
        )

        val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

        result ==== Collection(List(commentsInResponse(1)), None).good
      }

      "builds the next href if the backing client returns a next_href" in new Context {
        when(moshimoshiCommentsClient.fetchTrackComments(any, any, any))
          .thenReturn(
            Future.value(
              MoshimoshiCommentsPagedResponse(Seq.empty, Some("http://the-next-href-of-your-dreams.com")).good
            )
          )

        when(okidokiClient.fetchUsersMap(anonymousSession, Set.empty))
          .thenReturn(Future(Map.empty[Urn, UserRepresentation]))

        val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

        result ==== Collection(List[Comment](), Some(pagination.nextPage.normalizedHref)).good
      }
    }

    "When the rollout is enabled" >> {
      trait CommentsNewContext extends Context {
        rollout.isActive(commentsvasTrackComments) returns Future.value(true)

        when(moshimoshiCommentsClient.fetchTrackComments(any, any, any)).thenReturn(
          Future(
            MoshimoshiCommentsPagedResponse(moshimoshiCommentsResponse, None).good
          )
        )

        when(tracksTwirpClient.getTrackComments(any))
          .thenReturn(Future.value(GetTrackCommentsResponse(commentsFromVAS.map(_.urn))))

        when(commentsTwirpClient.getComments(any))
          .thenReturn(Future.value(GetCommentsResponse(commentsFromVAS)))

        when(
          okidokiClient.fetchUsersMap(
            any,
            any,
            any
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

      }
      "only calls comments VAS" in new CommentsNewContext {
        val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

        there was one(tracksTwirpClient).getTrackComments(any)
        there was one(commentsTwirpClient).getComments(any)
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
          .thenReturn(Future.value(GetTrackCommentsResponse(commentsFromVAS.map(_.urn))))

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
          .thenReturn(Future.value(GetTrackCommentsResponse(commentsFromVAS.map(_.urn))))

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

        val result =
          Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination, secretToken).value)

        result ==== Collection(commentsInResponse.toList, None).good
      }

      "filters out comments where the user could not be fetched" in new CommentsNewContext {
        val users = Set(UserBuilder.urnFor(20), UserBuilder.urnFor(21), UserBuilder.urnFor(22))
        when(okidokiClient.fetchUsersMap(anonymousSession, users)).thenReturn(
          Future.value(Map(UserBuilder.urnFor(21) -> UserBuilder.user(21)))
        )

        val result =
          Await.result(
            commentService.fetchTracksComments(anonymousSession, trackUrn, pagination, Some("secret-token")).value
          )

        result ==== Collection(
          Seq(commentFromVAS2).map(CommentFromVAS.fromProto).map(toResponseRepresentation).toList,
          None
        ).good
      }

      "builds the next href if the backing client returns a next_href" in new CommentsNewContext {
        when(moshimoshiCommentsClient.fetchTrackComments(any, any, any))
          .thenReturn(
            Future.value(
              MoshimoshiCommentsPagedResponse(
                moshimoshiCommentsResponse,
                Some("http://the-next-href-of-your-dreams.com")
              ).good
            )
          )

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

        result ==== Collection(commentsInResponse.toList, Some(pagination.nextPage.normalizedHref)).good
      }
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
