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
    user: User
)

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
      "uri" -> s"https://api.soundcloud.com/comments/${comment.id}"
    )
  }
}
