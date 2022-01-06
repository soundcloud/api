package com.soundcloud.apipublic.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.outcome.{CustomError, GoodOps, NotAllowed, NotValid, Outcome, OutcomeF}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.client.chrono.ChronoResponse
import com.soundcloud.apipublic.client.moshimoshicomments.MoshimoshiCommentsComment
import com.soundcloud.apipublic.client.mothership.response.mapper.{
  UpdatePlaylistArtworkResponseMapper,
  UserRepresentationMapper
}
import com.soundcloud.apipublic.client.mothership.response.representation.{UserRepresentation, WebProfile}
import com.soundcloud.apipublic.client.support.FetchClient
import com.soundcloud.apipublic.client.support.ResponseHandlers.ListResponse
import com.soundcloud.apipublic.handler.comments.CreateCommentParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.twitter.finagle.http.Status
import com.twitter.util.{Future, Try}
import play.api.libs.json.{JsNull, JsValue, Json, Writes}

import scala.util.control.NonFatal

class MoshimoshiClient(
    service: JsonClient,
    exceptionCollector: ExceptionCollector,
    updatePlaylistArtworkResponseMapper: UpdatePlaylistArtworkResponseMapper = new UpdatePlaylistArtworkResponseMapper
) extends FetchClient {

  def resolveToUrn(session: UserSession, permalink: String): Future[Option[Urn]] =
    service
      .getWithSession(session, Path() / "resolve_to_self", Params("permalink_url" -> permalink), Headers.empty())
      .map { response =>
        response.status match {
          case Status.Ok => Some((Json.parse(response.contentString) \ "self" \ "urn").as[Urn])
          case _ => None
        }
      }

  def fetchUserObjects(session: UserSession, urns: Set[Urn]): Future[List[UserRepresentation]] =
    fetchByUrns(service, session, Path() / "users" / "fetch", urns).map(_.map(UserRepresentationMapper(_)))

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

  def userWebProfiles(
      session: UserSession,
      userUrn: Urn
  ): Future[List[WebProfile]] = {
    service
      .getWithSession(
        session,
        Path("/users") / userUrn.identifier / "web_profiles",
        Params.empty,
        Headers.empty
      )
      .map(ListResponse(_).map(_.as[WebProfile]))
  }

  def updatePlaylistArtwork(
      session: UserSession,
      playlistUrn: Urn,
      requestParams: PlaylistArtworkUpdate
  ): OutcomeF[Unit] = {
    service
      .putWithSession(
        session,
        Path() / "playlists" / playlistUrn / "artwork",
        Params.empty,
        Headers.empty(),
        Some(Json.stringify(implicitly[Writes[PlaylistArtworkUpdate]].writes(requestParams)))
      )
      .map(updatePlaylistArtworkResponseMapper(_))
      .outcomeF
  }

  protected def parseRateLimitedError(errorJson: JsValue): Option[RateLimitedError] =
    for {
      spamWarningUrn <- (errorJson \ "spam_warning_urn").validate[String].asOpt
      urn <- Urn.parse(spamWarningUrn).toOption
    } yield RateLimitedError(urn)
}
