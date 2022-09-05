package com.soundcloud.apipublic.service.comments

import com.soundcloud.apipublic.client.comments.{Comment, CommentFromVAS, CommentsClient}
import com.soundcloud.apipublic.client.mothership.RichOkidokiClient
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.tracks.{CreateTrackCommentUserHasSpamWarning, TracksClient}
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps.JodaDateTimeExt
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone, LocalDateTime}
import org.mockito.Mockito.when
import proto.soundcloud.comments.api.{Comment => ProtoComment}

import scala.util.Try

class CommentServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val okidokiClient = mock[RichOkidokiClient]
    val tracksClient = mock[TracksClient]
    val commentsClient = mock[CommentsClient]

    val trackId = 4876
    val trackUrn = Urn("soundcloud", "tracks", trackId.toString())

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
    val commentService =
      new CommentService(
        okidokiClient,
        tracksClient,
        commentsClient
      )

  }

  "#fetchTrackComments" >> {

    trait CommentsNewContext extends Context {

      when(tracksClient.getComments(any, any, any, any, any))
        .thenReturn(commentsFromVAS.map(comment => Urn.parse(comment.urn).get).goodF)

      when(commentsClient.getComments(any))
        .thenReturn(commentsFromVAS.map(CommentFromVAS.fromProto).goodF)

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

      there was one(tracksClient).getComments(any, any, any, any, any)
      there was one(commentsClient).getComments(any)
    }

    "returns NotValid if the request to tracksClient returns Invalid argument" in new CommentsNewContext {
      when(tracksClient.getComments(any, any, any, any, any)).thenReturn(NotValid("").badF)

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== NotValid("").bad

    }

    "returns empty comments list if tracksClient returns empty list" in new CommentsNewContext {
      when(tracksClient.getComments(any, any, any, any, any)).thenReturn(Seq.empty.goodF)
      when(commentsClient.getComments(any)).thenReturn(Seq.empty.goodF)

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result ==== Collection(Seq[Comment]().toList, None).good
    }

    "throws exception if the tracksClient is throwing a RuntimeException" in new CommentsNewContext {
      when(tracksClient.getComments(any, any, any, any, any))
        .thenReturn(UnexpectedError(new RuntimeException("internal server error")).badF)
      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)

      result must beLeft.like {
        case UnexpectedError(throwable) =>
          throwable.getMessage must be_===(
            "internal server error"
          )
      }
    }

    "throws exception when the commentsClient throws a RuntimeException" in new CommentsNewContext {
      when(tracksClient.getComments(any, any, any, any, any))
        .thenReturn(commentsFromVAS.map(comment => Urn.parse(comment.urn).get).goodF)

      when(commentsClient.getComments(any))
        .thenReturn(
          UnexpectedError(new RuntimeException("unexpected response from comments: internal server error")).badF
        )

      val result = Await.result(commentService.fetchTracksComments(anonymousSession, trackUrn, pagination).value)
      result must beLeft.like {
        case UnexpectedError(throwable) =>
          throwable.getMessage must be_===(
            "unexpected response from comments: internal server error"
          )
      }
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

  "#createComment" >> {

    trait TrackCommentsCreateContext extends Context {
      val urn = Urn("soundcloud", "comments", "1")
      val comment = CommentFromVAS.fromProto(commentFromVAS1)
      val user = UserBuilder.user(comment.user.identifier.toLong)
      val createdAt: DateTime = LocalDateTime.now().toDateTime(DateTimeZone.UTC)

      lazy val createCommentResponse = urn.goodF
      when(tracksClient.createComment(any, any))
        .thenReturn(createCommentResponse)

      when(okidokiClient.fetchUsersMap(anonymousSession, Set(anonymousSession.getUser)))
        .thenReturn(Future(Map(anonymousSession.getUser -> user)))

      val createCommentParams =
        CreateCommentParams(comment.track, comment.body, comment.timestamp.map(s => s.toInt), comment.secretToken)
      lazy val result =
        Await.result(commentService.createComment(anonymousSession, createCommentParams, createdAt).value)
    }

    "returns Comment on success" in new TrackCommentsCreateContext {
      result ==== Comment.fromVASComment(urn, createCommentParams, createdAt, user).good
    }

    "forwards failures" >> {
      "failures while creating the comment" >> {
        "returns NotFound when tracksClient returns NotFound" in new TrackCommentsCreateContext {
          override lazy val createCommentResponse = NotFound("Track not found").badF

          result ==== NotFound("Track not found").bad
        }

        "returns NotValid when tracksClient returns InvalidArgument" in new TrackCommentsCreateContext {
          override lazy val createCommentResponse = NotValid("User is muted").badF
          result ==== NotValid("User is muted").bad
        }

        "returns NoAllowed when tracksClient returns PermissionDenied" in new TrackCommentsCreateContext {
          override lazy val createCommentResponse = NotAllowed("Track not commentable").badF
          result ==== NotAllowed("Track not commentable").bad
        }

        "returns CustomError when tracksClient returns ResourceExhausted" in new TrackCommentsCreateContext {
          lazy val spamWarningUrn = Urn.parse("soundcloud:spam-warnings:111").get
          override lazy val createCommentResponse =
            CustomError(CreateTrackCommentUserHasSpamWarning(spamWarningUrn)).badF
          result ==== CustomError(CreateTrackCommentUserHasSpamWarning(spamWarningUrn)).bad
        }

        "returns UnexpectedError when tracksClient returns an exception" in new TrackCommentsCreateContext {
          override lazy val createCommentResponse = UnexpectedError(new RuntimeException("Some Internal Error")).badF

          result must beLeft.like {
            case UnexpectedError(throwable) =>
              throwable.getMessage must be_===(
                "Some Internal Error"
              )
          }
        }
      }

      "failures while fetching the user" >> {
        "returns NotFound when okidoki returns an empty map" in new TrackCommentsCreateContext {
          when(okidokiClient.fetchUsersMap(anonymousSession, Set(anonymousSession.getUser)))
            .thenReturn(Future.value(Map.empty[Urn, UserRepresentation]))

          result ==== NotFound().bad
        }
      }
    }
  }

}
