package com.soundcloud.publicApiStrangler.client.comments

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User
import play.api.libs.json.{Json, Writes}

case class Comment(
    id: Long,
    body: String,
    createdAt: String,
    timestamp: Option[Int],
    trackId: Long,
    userId: Long,
    user: User,
    secretToken: Option[String] = None
) {
  def uri: String = {
    val maybeToken = secretToken.map(secretTokenParam).getOrElse("")
    location + maybeToken
  }

  def location = s"https://api.soundcloud.com/comments/$id"

  private def secretTokenParam(token: String) = s"?secret_token=$token"
}

object Comment {

  implicit val writes = Writes[Comment] { comment =>
    Json.obj(
      "kind" -> "comment",
      "id" -> comment.id,
      "body" -> comment.body,
      "created_at" -> comment.createdAt,
      "timestamp" -> comment.timestamp,
      "track_id" -> comment.trackId,
      "user_id" -> comment.userId,
      "user" -> comment.user,
      "uri" -> comment.uri
    )
  }

  def fromOkidokiComment(
      moshimoshiComment: MoshimoshiCommentsComment,
      user: User,
      secretToken: Option[String] = None
  ): Comment =
    Comment(
      id = moshimoshiComment.self.urn.identifier.toLong,
      body = moshimoshiComment.body,
      createdAt = moshimoshiComment.created_at,
      timestamp = moshimoshiComment.timestamp,
      trackId = moshimoshiComment.track.identifier.toLong,
      userId = moshimoshiComment.user.self.urn.identifier.toLong,
      user = user,
      secretToken = secretToken
    )
}
