package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http._
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.soundcloud.service.client.OkidokiClient
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
  implicit val reads = new Reads[TrackAudioMetadata]{
    override def reads(json: JsValue): JsResult[TrackAudioMetadata] =
      try {
        JsSuccess(
          TrackAudioMetadata(
            state = (json \ "state").as[String],
            original_format = (json \ "original_content_size").asOpt[String],
            original_content_size = (json \ "original_format").asOpt[Long]
          )
        )
      } catch {
        case ex: Exception => JsError(ex.getMessage)
      }

  }
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
}
