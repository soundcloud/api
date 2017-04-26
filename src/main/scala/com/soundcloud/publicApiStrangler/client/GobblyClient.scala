package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.module.http.client.{HttpClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import com.soundcloud.publicApiStrangler.client.gobbly.{Error, Result, ServerError, Success}
import com.twitter.finagle.http.Status
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

    client.getWithSession(session, Path() / "soundcloud-tracks", Params("urns" -> trackUrns), Headers.empty).map { response =>
      response.status match {
        case Status.Ok => Success(Json.parse(response.contentString).as[List[Urn]])
        case _ => errorResponse
      }
    } handle {
      case NonFatal(_) => errorResponse
    }
  }
}

object GobblySystem {
  val agent = new Urn("soundcloud:systems:187774")
}
