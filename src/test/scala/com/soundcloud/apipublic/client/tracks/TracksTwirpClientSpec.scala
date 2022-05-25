package com.soundcloud.apipublic.client.tracks

import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.verify
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.tracks.api.CreateTrackCommentErrorReasons.{
  TRACK_NOT_COMMENTABLE,
  USER_IS_MUTED_BY_MENTIONED_USERS,
  USER_IS_MUTED_BY_TRACK_OWNER
}
import proto.soundcloud.tracks.api._

class TracksTwirpClientSpec extends Specification with Mockito {

  trait Context extends Scope {

    val commentsClient = mock[CommentsClientProtobuf]
    val tracksTwirpClient = new TracksTwirpClient(commentsClient)
    val userUrn = Urn("soundcloud", "users", "1")
    val trackUrn = Urn("soundcloud", "tracks", "1")
    val session =
      new UserSessionBuilder().setUser(userUrn).build
  }

  "#createComment" >> {

    trait CreateCommentContext extends Context {

      val urn = Urn("soundcloud", "comments", "1")
      val params = CreateCommentParams(trackUrn, "Nice Track", Some(1000), None)

      val createCommentRequest =
        CreateTrackCommentRequest(
          Some(session.asProtoSession),
          trackUrn.toString,
          None,
          "Nice Track",
          Some(1000),
          Some(Const.REFERER)
        )

    }

    "createComment successfully creates comment" in new CreateCommentContext {
      commentsClient.createTrackComment(any) returns Future.value(
        CreateTrackCommentResponse(urn.toString)
      )

      val result =
        Await.result(
          tracksTwirpClient.createComment(session, params).value
        )
      result ==== urn.good

      val expectedRequest = createCommentRequest
      verify(commentsClient).createTrackComment(expectedRequest)
    }

    "createComment successfully creates comment with referer header" in new CreateCommentContext {
      val sessionWithReferer =
        new UserSessionBuilder().setUser(userUrn).setExtraHeader(UserSession.REFERER, "https://soundcloud.com/").build

      commentsClient.createTrackComment(any) returns Future.value(
        CreateTrackCommentResponse(urn.toString)
      )

      val result =
        Await.result(
          tracksTwirpClient.createComment(sessionWithReferer, params).value
        )
      result ==== urn.good

      val expectedRequest =
        CreateTrackCommentRequest(
          Some(session.asProtoSession),
          trackUrn.toString,
          None,
          "Nice Track",
          Some(1000),
          Some("https://soundcloud.com/")
        )

      verify(commentsClient).createTrackComment(expectedRequest)
    }

    "createComment fails" >> {
      "returns NotFound when commentsClient returns NotFound" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(ErrorCode.NotFound, "Track not found")
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== NotFound("Track not found").bad

        val expectedRequest = CreateTrackCommentRequest(
          userSession = Some(session.asProtoSession),
          urn = trackUrn.toString,
          secretToken = None,
          body = "Nice Track",
          timestamp = Some(1000),
          Some(Const.REFERER)
        )
        verify(commentsClient).createTrackComment(expectedRequest)
      }

      s"returns NotValid when commentsClient returns PermissionDenied with reason '$USER_IS_MUTED_BY_TRACK_OWNER'" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(
            ErrorCode.PermissionDenied,
            "User is muted",
            Map("reason" -> USER_IS_MUTED_BY_TRACK_OWNER.toString())
          )
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== NotValid("User is muted").bad

        verify(commentsClient).createTrackComment(any)
      }

      s"returns NotValid when commentsClient returns PermissionDenied with reason '$USER_IS_MUTED_BY_MENTIONED_USERS'" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(
            ErrorCode.PermissionDenied,
            "User is muted",
            Map("reason" -> USER_IS_MUTED_BY_MENTIONED_USERS.toString())
          )
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== NotValid("User is muted").bad

        verify(commentsClient).createTrackComment(any)
      }

      s"returns NotAllowed when commentsClient returns PermissionDenied with reason '$TRACK_NOT_COMMENTABLE'" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(
            ErrorCode.PermissionDenied,
            "Track not commentable",
            Map("reason" -> TRACK_NOT_COMMENTABLE.toString())
          )
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== NotAllowed("Track not commentable").bad

        verify(commentsClient).createTrackComment(any)
      }

      "returns NotAllowed when commentsClient returns PermissionDenied no reason" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(ErrorCode.PermissionDenied, "Not allowed to comment")
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== NotAllowed("Not allowed to comment").bad

        verify(commentsClient).createTrackComment(any)
      }

      "returns CustomError when commentsClient returns ResourceExhausted" in new CreateCommentContext {
        val spamWarningUrn = Urn.parse("soundcloud:spam-warnings:111").get
        val meta: Map[String, String] = Map("spam_warning_urn" -> spamWarningUrn.toString)
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(ErrorCode.ResourceExhausted, "too many requests", meta)
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result ==== CustomError(CreateTrackCommentUserHasSpamWarning(spamWarningUrn)).bad

        verify(commentsClient).createTrackComment(any)
      }

      "returns UnexpectedError when commentsClient returns InvalidArgument" in new CreateCommentContext {
        commentsClient.createTrackComment(any) returns Future.exception(
          TwinagleException(ErrorCode.InvalidArgument, "Invalid request")
        )

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)

        result must beLeft[ApplicationError].like {
          case UnexpectedError(e) =>
            e.getMessage ==== "unexpected response from tracks: 'Invalid request'"
        }

        verify(commentsClient).createTrackComment(any)
      }

      "returns UnexpectedError when tracks responds with Internal error" in new CreateCommentContext {
        val exception = TwinagleException(ErrorCode.Internal, "request failed")
        commentsClient.createTrackComment(any) returns Future.exception(exception)

        val result =
          Await.result(tracksTwirpClient.createComment(session, params).value)
        result must beLeft[ApplicationError].like {
          case UnexpectedError(e) =>
            e.getMessage ==== s"unexpected response from tracks: '${exception.msg}'"
        }
      }

    }

  }

  "getComments" >> {
    trait GetCommentsContext extends Context {

      val urn = Urn("soundcloud", "comments", "1")
      val request = GetTrackCommentsRequest(Some(session.asProtoSession), trackUrn.toString, None)
    }

    "returns urns successfully when commentsClient returns urns" in new GetCommentsContext {
      commentsClient.getTrackComments(any) returns Future.value(
        GetTrackCommentsResponse(Seq(urn.toString))
      )

      val result =
        Await.result(
          tracksTwirpClient.getComments(session, trackUrn, None, 0, 0).value
        )
      result ==== Seq(urn).good

      val expectedRequest = request
      verify(commentsClient).getTrackComments(expectedRequest)
    }

    "returns an empty list of urns when commentsClient returns NotFound" in new GetCommentsContext {

      commentsClient.getTrackComments(any) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Track not found")
      )

      val result =
        Await.result(
          tracksTwirpClient.getComments(session, trackUrn, None, 0, 0).value
        )

      result ==== Seq().good
    }

    "returns an empty list of urns when commentsClient returns PermissionDenied" in new GetCommentsContext {

      commentsClient.getTrackComments(any) returns Future.exception(
        TwinagleException(ErrorCode.NotFound, "Track not found")
      )

      val result =
        Await.result(
          tracksTwirpClient.getComments(session, trackUrn, None, 0, 0).value
        )

      result ==== Seq().good
    }

    "returns an UnexpectedError when the commentsClient returns any other exception" in new GetCommentsContext {

      commentsClient.getTrackComments(any) returns Future.exception(
        TwinagleException(ErrorCode.Internal, "some internal error")
      )

      val result =
        Await.result(
          tracksTwirpClient.getComments(session, trackUrn, None, 0, 0).value
        )

      result must beLeft.like {
        case UnexpectedError(throwable) =>
          throwable.getMessage must be_===(
            "unexpected response from tracks: some internal error"
          )
      }
    }
  }
}
