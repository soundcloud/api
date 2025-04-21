package com.soundcloud.apipublic.client.comments

import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.support.HtmlSanitizer
import com.soundcloud.jvmkit.module.util.Urn
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{Json, Writes}
case class Comment(
    urn: Urn,
    body: String,
    createdAt: String,
    timestamp: Option[Int],
    trackUrn: Urn,
    user: UserRepresentation,
    secretToken: Option[String] = None
) {
  def uri: String = {
    val maybeToken = secretToken.map(secretTokenParam).getOrElse("")
    location + maybeToken
  }

  def userUrn: Urn = user.urn

  def id: Long = urn.identifier.toLong
  def userId: Long = userUrn.identifier.toLong

  def location = s"https://api.soundcloud.com/comments/$id"

  private def secretTokenParam(token: String) = s"?secret_token=$token"
}

object Comment {

  implicit val writes = Writes[Comment] { comment =>
    Json.obj(
      "kind" -> "comment",
      "id" -> comment.id,
      "urn" -> comment.urn.toString,
      "body" -> HtmlSanitizer.sanitize(comment.body),
      "created_at" -> comment.createdAt,
      "timestamp" -> comment.timestamp,
      "track_id" -> comment.trackUrn.identifier.toLong,
      "track_urn" -> comment.trackUrn.toString,
      "user_id" -> comment.userId,
      "user_urn" -> comment.userUrn.toString,
      "user" -> comment.user,
      "uri" -> comment.uri
    )
  }

  def fromVASComment(
      commentUrn: Urn,
      params: CreateCommentParams,
      createdAtVasValueOverride: DateTime,
      user: UserRepresentation
  ): Comment =
    Comment(
      urn = commentUrn,
      body = params.body,
      createdAt = format(createdAtVasValueOverride),
      trackUrn = params.trackUrn,
      user = user,
      timestamp = params.timestamp,
      secretToken = params.secretToken
    )

  private def format(dateTime: DateTime): String = {
    DateTimeFormat
      .forPattern("yyyy/MM/dd HH:mm:ss Z")
      .print(dateTime)
  }
}
