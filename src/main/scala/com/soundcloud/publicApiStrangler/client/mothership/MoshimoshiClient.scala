package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.ResponseHandlers.{JsValueResponse, ListResponse}
import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{PlaylistUpdate, TrackCreate, TrackUpdate, UserUpdate}
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserFeedsSettings.format
import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.client.support.{FetchClient, ResponseHandlers, ResponseMapper}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConversions._

case class CreatePlaylistParams(title: String, public: Boolean = true)

class MoshimoshiClient(service: JsonClient,
                       addToPlaylistResponseMapper: AddToPlaylistResponseMapper = new AddToPlaylistResponseMapper,
                       deleteFromPlaylistResponseMapper: DeleteFromPlaylistResponseMapper = new DeleteFromPlaylistResponseMapper,
                       createPlaylistResponseMapper: CreatePlaylistResponseMapper = new CreatePlaylistResponseMapper,
                       deletePlaylistResponseMapper: DeletePlaylistResponseMapper = new DeletePlaylistResponseMapper,
                       updatePlaylistResponseMapper: UpdatePlaylistResponseMapper = new UpdatePlaylistResponseMapper,
                       updateUserResponseMapper: UpdateUserResponseMapper = new UpdateUserResponseMapper,
                       resetUserPasswordResponseMapper: ResponseMapper[ResetUserPasswordResponse] = new ResetUserPasswordResponseMapper,
                       deleteUserResponseMapper: ResponseMapper[DeleteUserResponse] = new DeleteUserResponseMapper)
  extends FetchClient {
  private val WHITELISTED_HEADERS = Set("x-real-ip", "x-forwarded-for")

  def fetchTrack(session: UserSession, urn: Urn): Future[Option[Track]] = {
    fetch(service, session, Path() / "tracks" / urn, Params.empty, Headers.empty).
      map(ResponseHandlers.OptionalSingleItem(_) map (TrackMapper(_)))
  }

  def fetchPlaylists(session: UserSession, urns: Set[Urn]): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "playlists" / "fetch", urns)

  def fetchPlaylistTracks(session: UserSession, urn: Urn, limit: Int, offset: Int): Future[List[Track]] =
    fetch(service, session, Path() / "playlists" / urn / "tracks", Params("limit" -> limit, "offset" -> offset))
      .map(ListResponse(_).map(TrackMapper(_)))

  def fetchPlaylistTrackUrns(session: UserSession, playlistUrn: Urn): Future[List[Urn]] =
    fetch(service, session, Path() / "playlists" / playlistUrn / "tracks_with_pagination", Params("representation_type" -> "id"))
      .map(JsValueResponse(_)).map(PlaylistTrackUrnsMapper(_))

  def fetchTracks(session: UserSession, urns: Set[Urn]): Future[List[Track]] =
    fetchByUrns(service, session, Path() / "tracks" / "fetch", urns).map(_.map(TrackMapper(_)))

  def fetchUsers(session: UserSession, urns: Set[Urn]): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "users" / "fetch", urns)

  def fetchUserObjects(session: UserSession, urns: Set[Urn]): Future[List[User]] =
    fetchByUrns(service, session, Path() / "users" / "fetch", urns).map(_.map(UserMapper(_)))

  def fetchPlaylistObjects(session: UserSession, urns: Set[Urn]): Future[List[Playlist]] =
    fetchByUrns(service, session, Path() / "playlists" / "fetch", urns).map(_.map(PlaylistMapper(_)))

  def fetchWebProfiles(session: UserSession, userUrn: Urn): Future[List[WebProfile]] = {
    fetch(service, session, Path() / "users" / userUrn.identifier / "web_profiles")
      .map(ListResponse(_)).map(WebProfileMapper(_))
  }

  def updateTrack(session: UserSession, urn: Urn, trackUpdate: TrackUpdate, headers: Headers): Future[Result[Track]] = {
    service.putWithSession(
      session,
      Path() / "tracks" / urn,
      Params.empty,
      filterHeaders(headers),
      Some(Json.stringify(JsObject(Seq("track" -> Json.toJson(trackUpdate)))))
    ).map(TrackUpdateResponseMapper(_))
  }

  def createTrack(session: UserSession, trackCreate: TrackCreate, headers: Headers): Future[Result[Track]] = {
    service.postWithSession(
      session,
      Path() / "tracks",
      Params.empty,
      filterHeaders(headers),
      Some(Json.stringify(JsObject(Seq(("track" -> Json.toJson(trackCreate))))))
    ).map(TrackCreateResponseMapper(_))
  }

  private def filterHeaders(headers: Headers): Headers = {
    val elems = (for {
      key <- headers.entrySet().map(_.getKey) if WHITELISTED_HEADERS.contains(key.toLowerCase)
      value <- headers.getAll(key)
    } yield (key, value)).toSeq
    Headers(elems: _*)
  }

  /**
    * See https://github.com/soundcloud/soundcloud/blob/master/vendor/plugins/moshimoshi/app/controllers/moshi_moshi/track_geoblockings_controller.rb#L5
    */
  def fetchTrackGeoblockings(session: UserSession, trackUrn: Urn): Future[Option[Geoblockings]] =
    fetch(service, session, Path() / "tracks" / trackUrn / "geo_blockings")
      .map(ResponseHandlers.OptionalSingleItem(_).flatMap(GeoblockingMapper(_)))

  /**
    * See https://github.com/soundcloud/soundcloud/blob/master/vendor/plugins/moshimoshi/app/controllers/moshi_moshi/track_geoblockings_controller.rb#L13
    */
  def updateTrackGeoblockings(session: UserSession, trackUrn: Urn, geoblockingsUpdate: Option[Geoblockings]): Future[Option[Geoblockings]] =
    service.putWithSession(
      session,
      Path() / "tracks" / trackUrn / "geo_blockings",
      Params.empty,
      Headers.empty,
      Some(Json.obj("geo_blockings" -> geoblockingsUpdate).toString)
    ).map(ResponseHandlers.OptionalSingleItem(_).flatMap(GeoblockingMapper(_)))

  def fetchFeatureStatus(session: UserSession, userUrn: Urn, name: String): Future[FeatureStatus] = {
    service.getWithSession(
      session,
      Path() / "users" / userUrn / "features" / name / "status",
      Params.empty,
      Headers.empty
    ).map(ResponseHandlers.OptionalSingleItem(_) match {
      case Some(_) => FeatureStatus(name, true)
      case None => FeatureStatus(name, false)
    })
  }

  def trackPurchaseLinks(session: UserSession, urns: Set[Urn]): Future[List[TrackPurchaseLink]] =
    fetchByUrns(service, session, Path() / "tracks" / "purchase_links", urns)
      .map(_.map(_.as[TrackPurchaseLink]))

  def addTrackToPlaylist(session: UserSession, playlistUrn: Urn, trackUrn: Urn): Future[AddToPlaylistResponse] =
    service.putWithSession(
      session,
      Path() / "playlists" / playlistUrn / "tracks" / trackUrn / "add_track",
      Params.empty,
      Headers.empty,
      None
    ).map(addToPlaylistResponseMapper(_))

  def deleteTrackFromPlaylist(session: UserSession, playlistUrn: Urn, trackUrn: Urn): Future[DeleteFromPlaylistResponse] =
    service.deleteWithSession(
      session,
      Path() / "playlists" / playlistUrn / "tracks" / trackUrn / "remove_track",
      Params.empty,
      Headers.empty,
      None
    ).map(deleteFromPlaylistResponseMapper(_))

  def createPlaylist(session: UserSession, params: CreatePlaylistParams): Future[Playlist] =
    service.postWithSession(
      session,
      Path() / "playlists",
      Params("title" -> params.title, "public" -> params.public.toString),
      Headers.empty,
      None
    ).map(createPlaylistResponseMapper(_))

  def updatePlaylist(session: UserSession, playlistUrn: Urn, playlistUpdate: PlaylistUpdate): Future[UpdatePlaylistResponse] = {
    service.putWithSession(
      session,
      Path() / "playlists" / playlistUrn,
      Params.empty,
      Headers.empty,
      Some(Json.toJson(playlistUpdate).toString)
    ).map(updatePlaylistResponseMapper(_))
  }

  def deletePlaylist(session: UserSession, playlistUrn: Urn): Future[DeletePlaylistResponse] = {
    service.deleteWithSession(
      session,
      Path() / "playlists" / playlistUrn,
      Params.empty,
      Headers.empty,
      None
    ).map(deletePlaylistResponseMapper(_))
  }

  def updateUserFeedsSettings(session: UserSession, userUrn: Urn, userFeedsSettings: UserFeedsSettings): Future[UserFeedsSettings] = {
    service.putWithSession(
      session,
      Path() / "users" / userUrn / "feeds_settings",
      Params.empty,
      Headers.empty,
      Some(UntypedJson.write(userFeedsSettings).toString)
    ).map(ResponseHandlers.SingleItem(_).as[UserFeedsSettings])
  }

  def fetchUserFeedsSettings(session: UserSession, userUrn: Urn): Future[Option[UserFeedsSettings]] = {
    service.getWithSession(
      session,
      Path() / "users" / userUrn / "feeds_settings",
      Params.empty,
      Headers.empty
    ).map(ResponseHandlers.OptionalSingleItem(_).map(_.as[UserFeedsSettings]))
  }

  def updateUser(session: UserSession, userUrn: Urn, userUpdate: UserUpdate): Future[UpdateUserResponse] =
    service.putWithSession(
      session,
      Path() / "users" / userUrn,
      Params.empty,
      Headers.empty,
      Some(Json.stringify(Json.toJson(userUpdate)))
    ).map(updateUserResponseMapper(_))

  def resetUserPassword(session: UserSession, email: String): Future[ResetUserPasswordResponse] =
    service.postWithSession(
      session,
      Path() / "users" / "password_reset",
      Params("email" -> email),
      Headers.empty,
      None
    ).map(resetUserPasswordResponseMapper(_))

  def resetUserPassword(session: UserSession, userUrn: Urn): Future[ResetUserPasswordResponse] =
    service.postWithSession(
      session,
      Path() / "users" / "password_reset",
      Params("user_id" -> userUrn.identifier),
      Headers.empty,
      None
    ).map(resetUserPasswordResponseMapper(_))

  def deleteUser(session: UserSession, userUrn: Urn, reason: Option[String] = None): Future[DeleteUserResponse] = {
    val reasonForDeletion: String = reason.getOrElse("")

    service.postWithSession(
      session,
      Path() / "purgatory",
      Params(
        "urn" -> userUrn,
        "actor_urn" -> session.getUser,
        "reason" -> reasonForDeletion
      ),
      Headers.empty,
      None
    ).map(deleteUserResponseMapper(_))
  }

  def resendEmailConfirmation(session: UserSession, userUrn: Urn, emailUrn: Urn): Future[Unit] = {
    val path = Path() / "users" / userUrn / "emails" / emailUrn / "confirmation"
    resendEmailConfirmationsWithRawPath(session, path)
  }

  def resendEmailConfirmationForAllUnconfirmedEmails(session: UserSession, userUrn: Urn): Future[Unit] = {
    val path = Path() / "users" / userUrn / "emails" / "unconfirmed" / "confirmation"
    resendEmailConfirmationsWithRawPath(session, path)
  }

  private def resendEmailConfirmationsWithRawPath(session: UserSession, path: Path): Future[Unit] = {
    service.postWithSession(session, path, Params.empty, Headers.empty, None)
      .map {
        case response if response.status == Status.ResetContent => Future.value(())
        case r => ResponseHandlers.invalidResponse(r)
      }
  }

}
