package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.{Config, ConfigConvention}
import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.client.CommonJsonFormats._
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.twitter.util.Future
import play.api.libs.json._

class TrackmetadataClient(service: JsonClient) {

  def track(session: UserSession, urn: Urn): Future[Option[Track]] = {
    service.get(session, Path() / "tracks" / urn, Params.empty, Params.empty).map {
      case JsonResponse(OkStatus, json, _, _) => Some(jsonToTrack(json))
      case _ => None
    }
  }

  private def jsonToTrack(json: JsValue): Track = (json \ "data").as[Track]

  def tracks(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[List[Track]] = {

    val batchedCalls = urns.grouped(batchSize).map {
      urnsGroup =>
        service.get(session, Path() / "tracks", toBigJvmKitUrnSet(urnsGroup), Params.empty).map {
          case JsonResponse(OkStatus, json, _, _) => jsonToTracks(json)
          case _ => Nil
        }
    }

    Future.collect(batchedCalls.toSeq).map(_.flatten.toList)
  }

  def urnsByUser(session: UserSession, userUrn: Urn): Future[List[Urn]] = {
    service.get(session, Path("/users") / userUrn / "tracks" / "urns", Params.empty, Params.empty).map {
      case JsonResponse(OkStatus, json, _, _) => (json \ "data").as[List[Urn]]
      // TODO lets please use results and actually return errors
      case _ => List.empty
    }
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
