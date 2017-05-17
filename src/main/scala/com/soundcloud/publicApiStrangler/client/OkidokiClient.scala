package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.ResponseHandlers.{JsValueResponse, OptionalSingleItem, SingleItem, UnitResponse}
import com.soundcloud.publicApiStrangler.mapper._
import com.soundcloud.publicApiStrangler.mapper.spotlight.SpotlightResponseMapper
import com.soundcloud.publicApiStrangler.representation._
import com.soundcloud.publicApiStrangler.representation.Email.reads
import com.soundcloud.publicApiStrangler.representation.spotlight.Spotlight
import com.soundcloud.publicApiStrangler.request.representation.{EmailCreate, EmailUpdate, TranscodingCreate}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json}

class OkidokiClient(service: JsonClient,
                    addToPlaylistResponseMapper: AddToPlaylistResponseMapper = new AddToPlaylistResponseMapper,
                    deleteFromPlaylistResponseMapper: DeleteFromPlaylistResponseMapper = new DeleteFromPlaylistResponseMapper,
                    createPlaylistResponseMapper: CreatePlaylistResponseMapper = new CreatePlaylistResponseMapper,
                    deletePlaylistResponseMapper: DeletePlaylistResponseMapper = new DeletePlaylistResponseMapper,
                    updatePlaylistResponseMapper: UpdatePlaylistResponseMapper = new UpdatePlaylistResponseMapper,
                    spotlightResponseMapper: SpotlightResponseMapper = new SpotlightResponseMapper)
  extends MoshimoshiClient(service, addToPlaylistResponseMapper, deleteFromPlaylistResponseMapper, createPlaylistResponseMapper, deletePlaylistResponseMapper, updatePlaylistResponseMapper) {

  def fetch(session: UserSession, urns: Set[Urn], batchSize: Int): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "fetch", urns, batchSize)

  def fetch(session: UserSession, urns: Set[Urn]): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "fetch", urns)

  def playlistTracks(session: UserSession, urn: Urn, limit: Option[Int] = None, after: Option[Int] = None): Future[TracksWithPagination] = {
    val params = (limit.map(v => Params("limit" -> v)) ++ after.map(v => Params("after" -> v))).flatten

    fetch(service, session, Path() / "playlists" / urn / "tracks_with_pagination", Params(params.toSeq: _*))
      .map(json => TracksWithPaginationMapper(SingleItem(json)))
  }

  def resolve(session: UserSession, permalinkUrl: String): Future[Option[JsObject]] = {
    val params = Params("permalink_url" -> permalinkUrl)
    service.getWithSession(session, Path("/resolve"), params, Headers.empty).map(OptionalSingleItem(_))
  }

  def fetchEmails(session: UserSession, userUrn: Urn): Future[List[Email]] = {
    service.getWithSession(session, Path() / "users" / userUrn / "emails", Params.empty, Headers.empty).map(JsValueResponse(_).as[List[Email]])
  }

  def deleteEmail(session: UserSession, userUrn: Urn, emailUrn: Urn): Future[Unit] = {
    service.deleteWithSession(session, Path() / "users" / userUrn / "emails" / emailUrn, Params.empty, Headers.empty, None)
      .map(UnitResponse(_))
  }

  def updateEmail(session: UserSession, userUrn: Urn, emailUrn: Urn, email: EmailUpdate): Future[Result[Email]] = {
    service.putWithSession(session, Path() / "users" / userUrn / "emails" / emailUrn, Params.empty, Headers.empty, Some(Json.stringify(Json.toJson(email))))
      .map { response: Response =>
        response.status match {
          case Status.Ok => Success(Json.parse(response.contentString).as[Email])
          case Status.BadRequest => BadRequest(Result.parseErrors(Json.parse(response.contentString)))
          case _ => ResponseHandlers.invalidResponse(response)
        }
      }
  }

  def createEmail(session: UserSession, userUrn: Urn, email: EmailCreate): Future[Result[Email]] = {
    service.postWithSession(session, Path() / "users" / userUrn / "emails", Params.empty, Headers.empty, Some(Json.stringify(Json.toJson(email))))
      .map { response: Response =>
        response.status match {
          case Status.Created => Success(Json.parse(response.contentString).as[Email])
          case Status.BadRequest => BadRequest(Result.parseErrors(Json.parse(response.contentString)))
          case Status.UnprocessableEntity => BadRequest(Result.parseErrors(Json.parse(response.contentString)))
          case Status.Conflict => BadRequest(Seq(Error("An email associated with that address already exists", None)))
          case _ => ResponseHandlers.invalidResponse(response)
        }
      }
  }

  /**
    * Given a possibly blocked user returns whether the possible blocker has blocked them or not.
    */
  def fetchRestriction(session: UserSession, possibleBlocker: Urn, possiblyBlockedUser: Urn): Future[Option[UserResourceRestriction]] = {
    val url = Path() / "users" / possiblyBlockedUser.toString / "resource_restrictions" / possibleBlocker.toString
    service.getWithSession(session, url, Params.empty, Headers.empty).map(response => UserResourceRestriction.parse(Json.parse(response.contentString)))
  }

  def createTranscoding(session: UserSession, transcoding: TranscodingCreate): Future[Result[TranscodingResponse]] = {
    val data = Json.obj("transcoding" -> transcoding).toString()
    service.postWithSession(session, Path() / "transcodings", Params.empty, Headers.empty, Some(data)).map {
      response: Response =>
        response.status match {
          case Status.Ok => Success(Json.parse(response.contentString).as[TranscodingResponse])
          case Status.BadRequest => BadRequest(Nil) //TODO: parse any errors from response body
        }
    }
  }

  def spotlight(session: UserSession, user: Urn): Future[Spotlight] =
    service.getWithSession(session, Path() / "users" / user.getIdentifier / "spotlight", Params.empty, Headers.empty).map(spotlightResponseMapper(_))

}
