package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient

import com.soundcloud.jvmkit.config.{Config, ConfigConvention}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.{ResourceName, UserSession}
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params, ServiceEntryPoint}
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json._

class TrackmetadataClient(service: JsonClient) {

  def track(session: UserSession, urn: Urn, secretToken: Option[String] = None): Future[Option[Track]] = {
    val params = secretToken.map(t => Params("secret_token" -> t)).getOrElse(Params.empty)
    service.get(session, Path() / "tracks" / urn, params, Params.empty).map {
      case JsonResponse(OkStatus, json, _, _) => Some(jsonToTrack(json))
      case _ => None
    }
  }

  private def jsonToTrack(json: JsValue): Track = (json \ "data").as[Track]

  def tracks(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[List[Track]] = {

    val batchedCalls = urns.grouped(batchSize).map {
      urnsGroup =>
        service.get(session, Path() / "tracks", urnsGroup, Params.empty).map {
          case JsonResponse(OkStatus, json, _, _) => jsonToTracks(json)
          case _ => Nil
        }
    }

    Future.collect(batchedCalls.toSeq).map(_.flatten.toList)
  }

  private def jsonToTracks(json: JsValue): List[Track] = (json \ "data").as[List[Track]]
}

object TrackmetadataClient {
  def apply(config: Config, telemetry: Telemetry, customEntryPoint: Option[ServiceEntryPoint] = None) = {
    val resourceName = ResourceName("trackmetadata")
    val entryPoint = customEntryPoint.getOrElse(ServiceEntryPoint(config.get(resourceName, ConfigConvention.SRV_RECORD)))
    val jsonClient = JsonClient(resourceName, entryPoint, config, telemetry)

    new TrackmetadataClient(jsonClient)
  }
}
