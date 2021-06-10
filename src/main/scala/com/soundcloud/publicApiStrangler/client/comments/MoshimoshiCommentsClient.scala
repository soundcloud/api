package com.soundcloud.publicApiStrangler.client.comments

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json
import com.soundcloud.jvmkit.module.json.play.UrnFormat._

case class MoshimoshiCommentsSelf(urn: Urn)
object MoshimoshiCommentsSelf {
  implicit val reads = Json.reads[MoshimoshiCommentsSelf]
  implicit val writes = Json.writes[MoshimoshiCommentsSelf]
}

case class MoshimoshiCommentsCommentUser(self: MoshimoshiCommentsSelf)
object MoshimoshiCommentsCommentUser {
  implicit val reads = Json.reads[MoshimoshiCommentsCommentUser]
  implicit val writes = Json.writes[MoshimoshiCommentsCommentUser]
}

case class MoshimoshiCommentsComment(
    body: String,
    created_at: String,
    timestamp: Option[Int],
    track: Urn,
    self: MoshimoshiCommentsSelf,
    user: MoshimoshiCommentsCommentUser
)
object MoshimoshiCommentsComment {
  implicit val reads = Json.reads[MoshimoshiCommentsComment]
  implicit val writes = Json.writes[MoshimoshiCommentsComment]
}

case class MoshimoshiCommentsPagedResponse(collection: Seq[MoshimoshiCommentsComment], next_href: Option[String])
object MoshimoshiCommentsPagedResponse {
  implicit val reads = Json.reads[MoshimoshiCommentsPagedResponse]
}

class MoshimoshiCommentsClient(service: JsonClient) {
  def fetchTrackComments(
      session: UserSession,
      track: Urn,
      pagination: OffsetBasedPagination
  ): Future[Outcome[MoshimoshiCommentsPagedResponse]] = {
    val path = Path() / "tracks" / track.identifier / "comments"
    // Always add linked_partitioning to keep the response consistent.
    // Moshimoshi will return a flat list without this param.
    val serviceParams = pagination.getParams + ("linked_partitioning" -> "1")

    service
      .getWithSession(session, path, serviceParams, Headers.empty())
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[MoshimoshiCommentsPagedResponse].good
          case Status.NotFound => NotFound().bad
          case Status.BadRequest => NotValid(response.contentString).bad
          case _ => HttpServiceError(HttpResponseFields(response.statusCode)).bad
        }
      }
  }
}
