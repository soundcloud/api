package com.soundcloud.apipublic.client.mothership

import com.soundcloud.apipublic.client.chrono.ChronoResponse
import com.soundcloud.apipublic.client.mothership.response.mapper.{
  UpdatePlaylistArtworkResponseMapper,
  UpdateUserArtworkResponseMapper,
  UserRepresentationMapper
}
import com.soundcloud.apipublic.client.mothership.response.representation.{UserRepresentation, WebProfile}
import com.soundcloud.apipublic.client.support.FetchClient
import com.soundcloud.apipublic.client.support.ResponseHandlers.ListResponse
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.hocuspocus.Image
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.outcome.OutcomeF
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.{JsValue, Json, Writes}

class MoshimoshiClient(
    service: JsonClient,
    exceptionCollector: ExceptionCollector,
    updatePlaylistArtworkResponseMapper: UpdatePlaylistArtworkResponseMapper = new UpdatePlaylistArtworkResponseMapper,
    updateUserArtworkResponseMapper: UpdateUserArtworkResponseMapper = new UpdateUserArtworkResponseMapper
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
    fetchByUrns(service, session, Path() / "users" / "fetch", urns)
      .map(_.map(UserRepresentationMapper(_, loggedInUser = session.user, loggedinApplicaton = Some(session.getAgent))))

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

  def updateUserAvatar(session: UserSession, userUrn: Urn, image: Image): OutcomeF[Unit] = {
    val userProfileAvatarUpdate = UserProfileAvatarUpdate(
      image.contentType,
      image.resizeUrl,
      image.originUri,
      image.width,
      image.height
    )
    service
      .postWithSession(
        session,
        Path() / "users" / userUrn.toString / "avatar",
        Params.empty,
        Headers.empty(),
        Some(Json.toJson(userProfileAvatarUpdate).toString())
      )
      .map(updateUserArtworkResponseMapper(_))
      .outcomeF
  }

  protected def parseRateLimitedError(errorJson: JsValue): Option[RateLimitedError] =
    for {
      spamWarningUrn <- (errorJson \ "spam_warning_urn").validate[String].asOpt
      urn <- Urn.parse(spamWarningUrn).toOption
    } yield RateLimitedError(urn)
}
