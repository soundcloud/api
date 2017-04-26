package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.{Json, Reads}

class PubmeseClient(jsonClient: JsonClient) {
  def isrcForTrack(session: UserSession, trackUrn: Urn): Future[Option[Isrc]] = {
    isrcsForTracks(session, Set(trackUrn)).map(_.get(trackUrn))
  }

  def isrcsForTracks(session: UserSession, trackUrns: Set[Urn]): Future[Map[Urn, Isrc]] = {
    val requestBody = Some(Json.obj("track_urns" -> toBigJvmKitUrnSet(trackUrns)).toString())

    jsonClient.post(session, Path() / "tracks", Params.empty, Params.empty, requestBody).map {
      case JsonResponse(OkStatus, body, _, _) => body.as[List[TrackRepresentation]].map { t => t.track_urn -> Isrc(t.isrc) }.toMap
      case _ => Map.empty[Urn, Isrc]
    }.handle {
      case NonFatal(_) => Map.empty[Urn, Isrc]
    }
  }

  case class TrackRepresentation(track_urn: Urn, isrc: String)

  object TrackRepresentation {
    implicit val reads: Reads[TrackRepresentation] = Json.reads[TrackRepresentation]
  }

}
