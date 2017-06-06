package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.servicediscovery.ServiceEntryPoint
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.config.{Config, ConfigConvention}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, ResourceName, Urn}
import com.soundcloud.publicApiStrangler.client.support.CommonJsonFormats._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json._

class TrackmetadataClient(service: JsonClient) {

  def track(session: UserSession, urn: Urn): Future[Option[Track]] = {
    service.getWithSession(session, Path() / "tracks" / urn, Params.empty, Headers.empty).map { response: Response =>
      response.status match {
        case Status.Ok => Some(jsonToTrack(Json.parse(response.contentString)))
        case _ => None
      }
    }
  }

  private def jsonToTrack(json: JsValue): Track = (json \ "data").as[Track]

  def tracks(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[List[Track]] = {

    val batchedCalls = urns.grouped(batchSize).map {
      urnsGroup =>
        service.getWithSession(session, Path() / "tracks", urnsGroup, Headers.empty).map { response: Response =>
          response.status match {
            case Status.Ok => jsonToTracks(Json.parse(response.contentString))
            case _ => Nil
          }
        }
    }

    Future.collect(batchedCalls.toSeq).map(_.flatten.toList)
  }

  def urnsByUser(session: UserSession, userUrn: Urn): Future[List[Urn]] = {
    service.getWithSession(session, Path("/users") / userUrn / "tracks" / "urns", Params.empty, Headers.empty).map { response: Response =>
      response.status match {
        case Status.Ok => (Json.parse(response.contentString) \ "data").as[List[Urn]]
        case _ => List.empty
      }
    }
  }

  private def jsonToTracks(json: JsValue): List[Track] = (json \ "data").as[List[Track]]
}

object TrackmetadataClient {
  def apply(config: Config, telemetry: Telemetry, customEntryPoint: Option[ServiceEntryPoint] = None) = {
    val resourceName = ResourceName("trackmetadata")
    val entryPoint = customEntryPoint.getOrElse(ServiceEntryPoint(config.get(resourceName, ConfigConvention.SRV_RECORD)))
    val jsonClient = JsonClient(entryPoint, HttpClientConfig.from(resourceName, config), telemetry)

    new TrackmetadataClient(jsonClient)
  }
}
