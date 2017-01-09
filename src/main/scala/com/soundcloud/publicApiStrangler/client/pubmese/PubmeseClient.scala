package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.Urn.format
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}

class PubmeseClient(jsonClient: JsonClient) {
  def isrcForTrack(session: UserSession, trackUrn: Urn): Future[Option[Isrc]] = {
    jsonClient.get(session, Path() / "tracks" / trackUrn, Params.empty, Params.empty).map {
      case JsonResponse(OkStatus, body, _, _) => (body \ "isrc").as[Option[String]].map(Isrc(_))
      case _ => None
    }.handle {
      case NonFatal(ex) => None
    }
  }
}
