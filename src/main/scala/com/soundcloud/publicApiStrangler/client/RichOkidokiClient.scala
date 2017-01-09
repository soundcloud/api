package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http._
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.service.client.{OkidokiClient, ResponseHandlers}
import com.soundcloud.service.response.mapper._
import com.soundcloud.service.response.mapper.spotlight.SpotlightResponseMapper
import com.soundcloud.service.response.representation._
import com.twitter.util.Future
import play.api.libs.json._

case class DomainLocking(
  domain: String,
  urn: Urn,
  trackUrn: Urn)

object DomainLocking {
  implicit val reads: Reads[DomainLocking] = new Reads[DomainLocking] {
    def reads(json: JsValue): JsResult[DomainLocking] =
      try {
        JsSuccess(
          DomainLocking(
            domain = (json \ "domain").as[String],
            urn = new Urn((json \ "self" \ "urn").as[String]),
            trackUrn = new Urn((json \ "track_urn").as[String])
          )
        )
      } catch {
        case ex: Exception => JsError(ex.getMessage)
      }
  }
}

case class TrackAudioMetadata(
  state: String,
  original_format: Option[String],
  original_content_size: Option[Long]
)

object TrackAudioMetadata {
  implicit val reads = Json.reads[TrackAudioMetadata]
}

class RichOkidokiClient(service: JsonClient,
  addToPlaylistResponseMapper: AddToPlaylistResponseMapper = new AddToPlaylistResponseMapper,
  deleteFromPlaylistResponseMapper: DeleteFromPlaylistResponseMapper = new DeleteFromPlaylistResponseMapper,
  createPlaylistResponseMapper: CreatePlaylistResponseMapper = new CreatePlaylistResponseMapper,
  deletePlaylistResponseMapper: DeletePlaylistResponseMapper = new DeletePlaylistResponseMapper,
  updatePlaylistResponseMapper: UpdatePlaylistResponseMapper = new UpdatePlaylistResponseMapper,
  spotlightResponseMapper: SpotlightResponseMapper = new SpotlightResponseMapper)
  extends OkidokiClient(
    service,
    addToPlaylistResponseMapper,
    deleteFromPlaylistResponseMapper,
    createPlaylistResponseMapper,
    deletePlaylistResponseMapper,
    updatePlaylistResponseMapper,
    spotlightResponseMapper) {
  def fetchTrackDomainLockings(session: UserSession, trackUrn: Urn): Future[Seq[DomainLocking]] =
    fetch(service, session, Path() / "tracks" / trackUrn.getIdentifier / "domain_lockings") map {
      case JsonResponse(OkStatus, body, _, _) => body.as[List[DomainLocking]]
      case _ => throw new RuntimeException("Unexpected response status")
    }

  def fetchTrackAudioMetadata(session: UserSession, trackUrn: Urn): Future[Option[TrackAudioMetadata]] =
    fetch(service, session, Path() / "tracks" / trackUrn / "audio") map {
      case JsonResponse(OkStatus, body, _, _) => body.as[Option[TrackAudioMetadata]]
      case JsonResponse(NotFoundStatus, _, _, _) => None
      case _ => throw new RuntimeException("Unexpected response status")
    }

  def fetchTracksAudioMetadata(session: UserSession, trackUrns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, TrackAudioMetadata]] = {
    def parseJson(body: JsValue): List[(Urn, TrackAudioMetadata)] = {
      (body \ "collection").as[List[JsValue]].map(json => {
        ((json \ "track_urn").as[Urn] -> (json).as[TrackAudioMetadata])
      })
    }

    inBatches(trackUrns.toList, batchSize) { urnBatch => {
      service.get(session, Path() / "tracks" / "audio", Params("urns" -> urnBatch), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) => parseJson(body)
        case _ => List.empty
      }
    } }.map(_.toMap)
  }

  def fetchTrackGeoblockings(session: UserSession, trackUrns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, Geoblockings]] = {
    def parseJson(body: JsValue): List[(Urn, Geoblockings)] = {
      (body \ "collection").as[List[JsValue]].map(json => {
        ((json \ "track_urn").as[Urn] -> (json \ "geo_blockings").asOpt[List[String]].getOrElse(List.empty))
      })
    }

    inBatches(trackUrns.toList, batchSize) { urnBatch => {
      service.get(session, Path() / "tracks" / "geo_blockings", Params("urns" -> urnBatch), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) => parseJson(body)
        case _ => List.empty
      }
    } }.map(_.toMap)
  }
}
