package com.soundcloud.apipublic.client.comments

import com.soundcloud.jvmkit.module.outcome.{GoodOps, OutcomeF, UnexpectedError}
import com.soundcloud.twinagle.TwinagleException
import proto.soundcloud.comments.api.{CommentsClientProtobuf, GetCommentsRequest}
import com.soundcloud.jvmkit.module.util.Urn

class CommentsTwirpClient(commentsTwirpClient: CommentsClientProtobuf) extends CommentsClient {
  override def getComments(urns: Seq[Urn]): OutcomeF[Seq[CommentFromVAS]] = {
    if (urns.isEmpty) Seq.empty.goodF
    else {
      val request = GetCommentsRequest(
        urns = urns.map(_.toString)
      )
      commentsTwirpClient
        .getComments(request)
        .map(response => response.comments.map(CommentFromVAS.fromProto).good)
        .handle {
          case TwinagleException(_, msg, _, _) =>
            UnexpectedError(new RuntimeException(s"unexpected response from comments: $msg")).bad
        }
        .outcomeF
        .catchToUnexpectedError
    }
  }
}
