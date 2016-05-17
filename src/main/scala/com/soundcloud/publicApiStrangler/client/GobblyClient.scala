package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.client.gobbly.{Error, Result, ServerError, Success}
import com.soundcloud.scalakit.Urn.format
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{Future, NonFatal}

/**
 * Client for Gobbly
 * https://github.com/soundcloud/gobbly
 */
class GobblyClient(jsonClient: JsonClient) {
  def allTracksManagedByFeedsForWrite(session: UserSession, urns: List[Urn]): Future[Result[Boolean]] =
    tracksFromFeeds(session, urns).map(_.map(trackUrns => trackUrns.toSet == urns.toSet))

  private def tracksFromFeeds(session: UserSession, trackUrns: List[Urn]): Future[Result[List[Urn]]] = {
    val errorResponse = ServerError(Error("Error loading managed by feeds status from gobbly"))

    jsonClient.get(session, Path() / "soundcloud-tracks", Params("urns" -> trackUrns), Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => Success(body.as[List[Urn]])
      case JsonResponse(status, _, _, _) => errorResponse
    } handle {
      case NonFatal(e) => errorResponse
    }
  }
}

object GobblySystem {
  val agent = new Urn("soundcloud:systems:187774")
}
