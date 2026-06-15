package com.soundcloud.apipublic.client.recentlyplayed

import com.soundcloud.apipublic.client.support.UnhandledResponseException
import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Status.Successful
import com.twitter.util.Future
import play.api.libs.json.Json

class RecentlyPlayedClient(apiClient: JsonClient) {

  def getTracks(session: UserSession, user: Urn, limit: Int): Future[List[RecentlyPlayedTrack]] = {
    apiClient
      .getWithSession(session, Path() / "users" / user / "recently-played" / "tracks", Map("limit" -> limit.toString))
      .map { response =>
        response.status match {
          case Successful(_) => Json.parse(response.contentString).as[List[RecentlyPlayedTrack]]
          case _ => throw UnhandledResponseException(response)
        }
      }
  }
}
