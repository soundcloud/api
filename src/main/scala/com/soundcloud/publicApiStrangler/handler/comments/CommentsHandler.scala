package com.soundcloud.publicApiStrangler.handler.comments

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.handler.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.pagination.OffsetBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Duration, Future}
import com.twitter.conversions.DurationOps._
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}

class CommentsHandler(
    userAuthentication: UserAuthentication,
    commentService: CommentService,
    mothershipDispatcher: DispatchToMothershipHandler,
    telemetry: Telemetry
) {
  def getCommentsForTrack(request: HandlerRequest): Future[Response] = {
    Future
      .join(
        mothershipDispatcher.dispatch(request),
        performGetCommentsForTrack(request)
      )
      .map {
        case (mothershipResponse, pasResponse) => {
          val mothershipJson = jsValue(mothershipResponse.contentString)
          val pasJson = jsValue(pasResponse.contentString)

          compareAndReportComments(
            mothershipJson,
            pasJson
          )
          mothershipResponse
        }
      }
  }

  def performGetCommentsForTrack(request: HandlerRequest): Future[Response] = {
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

  private def jsValue(contentString: String): JsValue = {
    var mothershipJson: Option[JsValue] = Json.parse(contentString).asOpt[JsObject]
    mothershipJson match {
      case Some(_) => mothershipJson = mothershipJson
      case None => mothershipJson = Json.parse(contentString).asOpt[JsArray]
    }
    mothershipJson.get
  }

  private def compareAndReportComments(
      mothershipResponseValue: JsValue,
      pasResponseValue: JsValue
  ): Unit = {
    if (mothershipResponseValue != pasResponseValue) {
      inconsistentCommentsFetchResponsesCounter.inc()
      SoundCloudLoggerFactory
        .getLogger(getClass)
        .warn(s"Comment inconsistency: ${mothershipResponseValue} != ${pasResponseValue}")
    }
  }

  private val inconsistentCommentsFetchResponsesCounter =
    telemetry.counter(
      "inconsistent_comment_fetch_response_total",
      "Count of inconsistent (not matching) responses from legacy and new comments fetch"
    )

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
