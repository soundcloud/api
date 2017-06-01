package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{Json, Reads}

import scala.util.control.NonFatal

class PubmeseClient(jsonClient: JsonClient) {
  def isrcForTrack(session: UserSession, trackUrn: Urn): Future[Option[Isrc]] = {
    isrcsForTracks(session, Set(trackUrn)).map(_.get(trackUrn))
  }

  def isrcsForTracks(session: UserSession, trackUrns: Set[Urn]): Future[Map[Urn, Isrc]] = {
    val requestBody = Some(Json.obj("track_urns" -> trackUrns).toString())

    jsonClient.postWithSession(session, Path() / "tracks", Params.empty, Headers.empty(), requestBody).map { response: Response =>
      response.status match {
        case Status.Ok => Json.parse(response.contentString).as[List[TrackRepresentation]].map { t => t.track_urn -> Isrc(t.isrc) }.toMap
        case _ => Map.empty[Urn, Isrc]
      }
    }.handle {
      case NonFatal(_) => Map.empty[Urn, Isrc]
    }
  }

  case class TrackRepresentation(track_urn: Urn, isrc: String)

  object TrackRepresentation {
    implicit val reads: Reads[TrackRepresentation] = Json.reads[TrackRepresentation]
  }

}
