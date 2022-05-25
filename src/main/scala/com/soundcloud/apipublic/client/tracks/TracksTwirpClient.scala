package com.soundcloud.apipublic.client.tracks

import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import proto.soundcloud.tracks.api.CreateTrackCommentErrorReasons.{
  USER_IS_MUTED_BY_MENTIONED_USERS,
  USER_IS_MUTED_BY_TRACK_OWNER
}
import proto.soundcloud.tracks.api.{
  CreateTrackCommentRequest,
  GetTrackCommentsRequest,
  CommentsClientProtobuf => TracksCommentsClientProtoBuf
}
object Const {
  final val REFERER = "https://api.soundcloud.com/"
}

case class CreateTrackCommentUserHasSpamWarning(spamWarning: Urn)

class TracksTwirpClient(trackCommentsTwirpClient: TracksCommentsClientProtoBuf) extends TracksClient {

  override def createComment(session: UserSession, params: CreateCommentParams): OutcomeF[Urn] = {

    val refererHeader = Option(session.getExtraHeader(UserSession.REFERER))
    val referer = if (refererHeader.isDefined) refererHeader else Some(Const.REFERER)

    val request = CreateTrackCommentRequest(
      Some(session.asProtoSession),
      params.trackUrn.toString,
      params.secretToken,
      params.body,
      params.timestamp,
      referer
    )
    trackCommentsTwirpClient
      .createTrackComment(request)
      .map(response => Urn.parse(response.urn).get.good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, msg, _, _) =>
          NotFound(msg).bad
        case TwinagleException(ErrorCode.PermissionDenied, msg, meta, _) =>
          meta
            .get("reason")
            .map {
              case USER_IS_MUTED_BY_MENTIONED_USERS.name | USER_IS_MUTED_BY_TRACK_OWNER.name => NotValid(msg).bad
              case _ => NotAllowed(msg).bad
            }
            .getOrElse(NotAllowed(msg).bad)
        case TwinagleException(ErrorCode.ResourceExhausted, _, meta, _) if meta.contains("spam_warning_urn") =>
          val spamWarningUrn = Urn.parse(meta("spam_warning_urn")).get
          CustomError(CreateTrackCommentUserHasSpamWarning(spamWarningUrn)).bad
        case TwinagleException(_, msg, _, _) =>
          UnexpectedError(new RuntimeException(s"unexpected response from tracks: '$msg'")).bad
      }
      .outcomeF
      .catchToUnexpectedError
  }

  override def getComments(
      session: UserSession,
      trackUrn: Urn,
      secretToken: Option[String],
      offset: Int,
      limit: Int
  ): OutcomeF[Seq[Urn]] = {

    val request = GetTrackCommentsRequest(
      userSession = Some(session.asProtoSession),
      urn = trackUrn.toString,
      secretToken = secretToken,
      offset = offset,
      limit = limit
    )
    trackCommentsTwirpClient
      .getTrackComments(request)
      .map(response => response.commentsUrns.map(Urn.parse(_).get).good)
      .handle {
        case TwinagleException(ErrorCode.NotFound, _, _, _) | TwinagleException(ErrorCode.PermissionDenied, _, _, _) =>
          Seq.empty.good
        case TwinagleException(_, msg, _, _) =>
          UnexpectedError(new RuntimeException(s"unexpected response from tracks: $msg")).bad
      }
      .outcomeF
      .catchToUnexpectedError
  }
}
