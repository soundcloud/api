package com.soundcloud.apipublic.client.search

import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.Path
import com.twitter.finagle.http.Status
import play.api.libs.json.Json

class SearchDispatcherClient(jsonClient: JsonClient) extends SearchClient {
  import QueryMappers._

  def searchTracks(
      session: UserSession,
      params: TracksParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(session, Path() / "search" / "tracks", params.toParams, headers)

  def searchPlaylists(
      session: UserSession,
      params: PlaylistsParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(session, Path() / "search" / "playlists", params.toParams, headers)

  def searchUsers(
      session: UserSession,
      params: UsersParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] =
    doSearch(session, Path() / "search" / "users", params.toParams, headers)

  def search(
      session: UserSession,
      path: Path,
      params: SearchQueryParams,
      headers: Headers = Headers.empty,
      access: AccessParams
  ): OutcomeF[SearchResponse] = doSearch(session, Path() / "search" / "universal", params.toParams, headers)

  private def doSearch(
      session: UserSession,
      path: Path,
      params: Params,
      headers: Headers
  ): OutcomeF[SearchResponse] = {
    jsonClient
      .getWithSession(session, path, params, headers)
      .map { response =>
        response.status match {
          case Status.Ok => Json.parse(response.contentString).as[SearchResponse].good
          case Status.BadRequest => NotValid("invalid request").bad
          case Status.NotFound | Status.Unauthorized => NotFound().bad
          case _ => HttpServiceError(HttpResponseFields(response.statusCode)).bad
        }
      }
      .outcomeF
  }
}
