package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome.{CustomError, NotValid, Outcome, _}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.chrono.ChronoResponse
import com.soundcloud.publicApiStrangler.client.comments.MoshimoshiCommentsComment
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.soundcloud.publicApiStrangler.handler.comments.CreateCommentParams
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.twitter.finagle.http.Status
import com.twitter.util.{Future, Try}
import play.api.libs.json.{JsNull, JsValue, Json}

import scala.util.control.NonFatal

class MoshimoshiClient(
    service: JsonClient,
    exceptionCollector: ExceptionCollector
) extends FetchClient {

  def fetchUserObjects(session: UserSession, urns: Set[Urn]): Future[List[User]] =
    fetchByUrns(service, session, Path() / "users" / "fetch", urns).map(_.map(UserMapper(_)))

  def userPlaylists(
      session: UserSession,
      userUrn: Urn,
      pagination: CursorBasedPagination
  ): Future[ChronoResponse] = {
    val paramsWithoutCursor = Map(
      "limit" -> pagination.pageSize.toString,
      "direction" -> "desc"
    )
    val params = pagination.cursor
      .map(cursor => paramsWithoutCursor ++ Map("cursor" -> cursor))
      .getOrElse(paramsWithoutCursor)

    service
      .getWithSession(
        session,
        Path() / "users" / userUrn / "playlists" / "chrono",
        params,
        Headers.empty
      )
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[ChronoResponse]
          case _ => ChronoResponse.emptyResponse
        }
      }
  }

  def createComment(
      session: UserSession,
      createCommentParams: CreateCommentParams
  ): Future[Outcome[MoshimoshiCommentsComment]] = {
    val serviceParams = Params(
      "track_id" -> createCommentParams.trackUrn.identifier,
      "comment[body]" -> createCommentParams.body
    ) ++ createCommentParams.timestamp
      .map(ts => Params("comment[timestamp]" -> ts.toString))
      .getOrElse(Params.empty) ++ createCommentParams.secretToken
      .map(token => Params("secret_token" -> token))
      .getOrElse(Params.empty)

    service
      .postWithSession(session, Path() / "comments", serviceParams, Headers.empty, None)
      .map { response =>
        response.status match {
          case Status.Created => Json.parse(response.contentString).as[MoshimoshiCommentsComment].good
          case Status.UnprocessableEntity => CustomError(UnprocessableEntity).bad
          case Status.Unauthorized | Status.Forbidden => NotAllowed().bad
          case Status.TooManyRequests => {
            val rateLimitError = parseRateLimitedError(Try(Json.parse(response.contentString)).getOrElse(JsNull))
            CustomError(
              TooManyRequests,
              rateLimitError.map(CustomError(_))
            ).bad
          }
          case _ => NotValid("Something went wrong").bad
        }
      }
      .handleAndReport(exceptionCollector) {
        case NonFatal(_) =>
          NotValid("Something went wrong").bad
      }
  }

  protected def parseRateLimitedError(errorJson: JsValue): Option[RateLimitedError] =
    for {
      spamWarningUrn <- (errorJson \ "spam_warning_urn").validate[String].asOpt
      urn <- Urn.parse(spamWarningUrn).toOption
    } yield RateLimitedError(urn)
}
