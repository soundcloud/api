package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ModuleConversions._
import com.soundcloud.jvmkit.module.http.client.{Headers, HttpClient, HttpResponse, OkHttpStatus, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.client.gobbly.{Error, Result, ServerError, Success}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json._

/**
 * Client for Gobbly
 * https://github.com/soundcloud/gobbly
 */
class GobblyClient(client: HttpClient) {
  def allTracksManagedByFeedsForWrite(session: UserSession, urns: List[Urn]): Future[Result[Boolean]] =
    tracksFromFeeds(session, urns).map(_.map(trackUrns => trackUrns.toSet == urns.toSet))

  private def tracksFromFeeds(session: UserSession, trackUrns: List[Urn]): Future[Result[List[Urn]]] = {
    val errorResponse = ServerError(Error("Error loading managed by feeds status from gobbly"))

    client.getWithSession(session, Path() / "soundcloud-tracks", Params("urns" -> trackUrns.map(toModuleUrn)), Headers.empty).map {
      case HttpResponse(OkHttpStatus, body, _) => Success(Json.parse(body).as[List[Urn]])
      case HttpResponse(_, _, _) => errorResponse
    } handle {
      case NonFatal(_) => errorResponse
    }
  }
}

object GobblySystem {
  val agent = new Urn("soundcloud:systems:187774")
}
