package com.soundcloud.publicApiStrangler.handler.comments

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.conversions.DurationOps._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Duration, Future}

class CommentsHandler(
    userAuthentication: UserAuthentication,
    commentService: CommentService
) {

  def getCommentsForTrack(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) { session =>
      val track = Urn("soundcloud", "tracks", request.routeParams("trackId"))
      commentService.fetchTracksComments(session, track, pagination(request)).map {
        case Good(comments) => {
          val body = Collection.getNonNullRepresentation(comments, request.params.get("linked_partitioning").isDefined)
          JsonResponseBuilder(Status.Ok, body, buildCacheHeaders(Some(10.minutes))).build
        }
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(notFoundErrorString)
        case Bad(_) => JsonResponseBuilder.badRequest()
      }
    }
  }

  private def pagination(request: HandlerRequest): OffsetBasedPagination = {
    val params = request.params
    val limit = params.get("limit").map(_.toInt).getOrElse(200) // 200 limit to match the mothership behaviour
    val offset = params.get("offset").map(_.toInt)

    val basePagination =
      OffsetBasedPagination.build(request, request.params.keySet.toSeq)
    basePagination.copy(limit = limit, offset = offset)
  }

  private def buildCacheHeaders(duration: Option[Duration]): Map[String, String] =
    duration
      .map { maxAge =>
        Map("Cache-Control" -> s"max-age=${maxAge.inSeconds.toString}, must-revalidate")
      }
      .getOrElse(Map.empty)

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""
}
