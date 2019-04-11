package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json._

case class TrackRequests(trackRequests: List[TrackRequest])
case class TrackRequest(urn: Urn, secretToken: Option[String])

object TrackRequests {
  implicit val trackRequestWrites: Writes[TrackRequest] = Json.writes[TrackRequest]
  implicit val trackRequestsWrites: Writes[TrackRequests] = Json.writes[TrackRequests]
}

class TracksClient(jsonClient: JsonClient) {

  def visibleTrack(session: UserSession, urn: Urn, secretToken: Option[String]): Future[Option[VisibleTrack]] = {
    val body = Json.stringify(Json.toJson(TrackRequests(List(TrackRequest(urn, secretToken)))))
    jsonClient.postWithSession(session, Path() / "tracks", Params.empty, Headers.empty, Some(body)).map { response: Response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "data").as[List[VisibleTrack]].headOption
        case _ => throw new UnhandledResponseException(response)
      }
    }
  }

}
