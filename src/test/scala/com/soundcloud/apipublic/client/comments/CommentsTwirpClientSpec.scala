package com.soundcloud.apipublic.client.comments

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps.JodaDateTimeExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime
import org.mockito.Mockito.verify
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.comments.api.{
  CommentsClientProtobuf,
  GetCommentsRequest,
  GetCommentsResponse,
  Comment => ProtoComment
}

class CommentsTwirpClientSpec extends Specification with Mockito {

  trait Context extends Scope {

    val commentsClient: CommentsClientProtobuf = mock[CommentsClientProtobuf]
    val commentsTwirpClient = new CommentsTwirpClient(commentsClient)
  }

  "getComments" >> {
    trait GetCommentsContext extends Context {

      val urn = Urn("soundcloud", "comments", "1")
      val request = GetCommentsRequest(Seq(urn.toString))

      val commentFromVAS =
        ProtoComment(
          "soundcloud:comments:1",
          "soundcloud:tracks:4876",
          "soundcloud:users:20",
          "I am a comment. I represent opinion.",
          Some(JodaDateTimeExt(DateTime.parse("2017-08-23T15:25:14.154Z")).asProto),
          Some(1000)
        )

      val comment = CommentFromVAS.fromProto(commentFromVAS)
    }

    "returns comments successfully" in new GetCommentsContext {
      commentsClient.getComments(any) returns Future.value(
        GetCommentsResponse(Seq(commentFromVAS))
      )

      val result =
        Await.result(
          commentsTwirpClient.getComments(Seq(urn)).value
        )
      result ==== Seq(comment).good

      val expectedRequest = request
      verify(commentsClient).getComments(expectedRequest)
    }

    "getComments successfully returns empty" in new GetCommentsContext {
      commentsClient.getComments(any) returns Future.value(
        GetCommentsResponse(Seq.empty)
      )

      val result =
        Await.result(
          commentsTwirpClient.getComments(Seq.empty).value
        )
      result ==== Seq.empty.good
    }

    "returns UnexpectedError when the commentsClient returns any other exception  " in new GetCommentsContext {
      commentsClient.getComments(any) returns Future.exception(
        TwinagleException(ErrorCode.Internal, "some internal error")
      )

      val result =
        Await.result(
          commentsTwirpClient.getComments(Seq(urn)).value
        )

      result must beLeft.like {
        case UnexpectedError(throwable) =>
          throwable.getMessage must be_===(
            "unexpected response from comments: some internal error"
          )
      }
    }
  }
}
