package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.chrono.ChronoResponse
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class MoshimoshiClient(
    service: JsonClient
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

}
