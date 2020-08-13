package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json._

class TrackmetadataClient(service: JsonClient) {
  def urnsByUser(session: UserSession, userUrn: Urn): Future[List[Urn]] = {
    service.getWithSession(session, Path("/users") / userUrn / "tracks" / "urns", Params.empty, Headers.empty).map {
      response: Response =>
        response.status match {
          case Status.Ok => (Json.parse(response.contentString) \ "data").as[List[Urn]]
          case _ => List.empty
        }
    }
  }
}
