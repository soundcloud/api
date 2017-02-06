package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http._
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.service.client.{OkidokiClient, ResponseHandlers}
import com.soundcloud.service.response.mapper._
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser
import com.soundcloud.service.response.mapper.spotlight.SpotlightResponseMapper
import com.soundcloud.service.response.representation._
import com.soundcloud.bff.nextbff.mapping.MappingContext
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
  val FinishedState = "finished"

  implicit val reads = Json.reads[TrackAudioMetadata]
}

class RichOkidokiClient(service: JsonClient) extends OkidokiClient(service) {
  def fetchTrackDomainLockings(session: UserSession, trackUrn: Urn): Future[Seq[DomainLocking]] =
    fetch(service, session, Path() / "tracks" / trackUrn.getIdentifier / "domain_lockings") map {
      case JsonResponse(OkStatus, body, _, _) => body.as[List[DomainLocking]]
      case _ => throw new RuntimeException("Unexpected response status")
    }

  def fetchTracksDomainLockings(session: UserSession, trackUrns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, List[DomainLocking]]] = {
    inBatches(trackUrns.toList, batchSize) { urnBatch => {
      service.get(session, Path() / "domain_lockings", Params("track_ids" -> urnBatch.map(_.getIdentifier).mkString(",")), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) => body.as[List[DomainLocking]]
        case _ => List.empty
      }
    } }.map(_.groupBy(_.trackUrn))
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
      val path = Path() / "tracks" / "audio" // okidoki does unwanted magic without the trailing "/"
      service.get(session, path / "", Params("urns" -> urnBatch.mkString(",")), Params.empty).map {
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
      val path = Path() / "tracks" / "geo_blockings" / "" // okidoki does unwanted magic without the trailing "/"
      service.get(session, path, Params("urns" -> urnBatch.mkString(",")), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) => parseJson(body)
        case JsonResponse(_, body, _, _) => List.empty
        case _ => List.empty
      }
    } }.map(_.toMap)
  }

  def fetchUsersMap(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, User]] = {
    inBatches(urns.toList, batchSize) { urnBatch => {
      service.get(session, Path() / "users" / "fetch", Params("urns" -> urnBatch.mkString(",")), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) => body.as[List[JsValue]].map(UserMapper(_))
        case _ => List.empty
      }
    } }.map(_.map(user => (user.urn -> user)).toMap)
  }

  def fetchRepostsUsersWithoutCounts(session: UserSession, urns: Set[Urn], baseUrl: String, batchSize: Int = 50): Future[List[RepostsUser]] = {
    inBatches(urns.toList, batchSize) { urnBatch =>
      service.get(session, Path() / "users" / "fetch",
                  Params("urns" -> urnBatch), Params.empty).map {
        case JsonResponse(SuccessfulStatusClass(_), body, _, _) =>
          body.as[List[JsValue]].map { jsonUser =>
            RepostsUser(jsonUser, baseUrl, None, None, None)(new MappingContext(session))
          }
        case _ => List.empty
      }
    }
  }

}
