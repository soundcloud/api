package com.soundcloud.publicApiStrangler.client.pubmese

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.jvmkit.Urn.format
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.soundcloud.scalakit.Path
import com.twitter.util.Future

class PubmeseClient(jsonClient: JsonClient) {
  def isrcForTrack(session: UserSession, trackUrn: Urn): Future[Option[Isrc]] = {
    jsonClient.get(session, Path() / "tracks" / trackUrn).map {
      case JsonResponse(OkStatus, body, _, _) => (body \ "isrc").as[Option[String]].map(Isrc(_))
      case JsonResponse(NotFoundStatus, _, _, _) => None
      case _ => throw new RuntimeException("Unexpected response status")
    }
  }
}
