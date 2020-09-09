package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserMapper
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.twitter.finagle.http.Status.Successful
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json._

case class DomainLocking(domain: String, urn: Urn, trackUrn: Urn)

object DomainLocking {
  implicit val reads: Reads[DomainLocking] = (json: JsValue) =>
    try {
      JsSuccess(
        DomainLocking(
          domain = (json \ "domain").as[String],
          urn = (json \ "self" \ "urn").as[Urn],
          trackUrn = (json \ "track_urn").as[Urn]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
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
    fetch(service, session, Path() / "tracks" / trackUrn.identifier / "domain_lockings") map { response: Response =>
      response.status match {
        case Status.Ok => Json.parse(response.contentString).as[List[DomainLocking]]
        case _ => throw new RuntimeException("Unexpected response status")
      }
    }

  def fetchTracksDomainLockings(
      session: UserSession,
      trackUrns: Set[Urn],
      batchSize: Int = 50
  ): Future[Map[Urn, List[DomainLocking]]] = {
    inBatches(trackUrns.toList, batchSize) { urnBatch =>
      {
        service
          .getWithSession(
            session,
            Path() / "domain_lockings",
            Params("track_ids" -> urnBatch.map(_.identifier).mkString(",")),
            Headers.empty()
          )
          .map { response: Response =>
            response.status match {
              case Successful(_) => Json.parse(response.contentString).as[List[DomainLocking]]
              case _ => List.empty
            }
          }
      }
    }.map(_.groupBy(_.trackUrn))
  }

  def fetchTrackAudioMetadata(session: UserSession, trackUrn: Urn): Future[Option[TrackAudioMetadata]] =
    fetch(service, session, Path() / "tracks" / trackUrn / "audio") map { response: Response =>
      response.status match {
        case Status.Ok => Json.parse(response.contentString).asOpt[TrackAudioMetadata]
        case Status.NotFound => None
        case _ => throw new RuntimeException("Unexpected response status")
      }
    }

  def fetchTracksAudioMetadata(
      session: UserSession,
      trackUrns: Set[Urn],
      batchSize: Int = 50
  ): Future[Map[Urn, TrackAudioMetadata]] = {
    def parseJson(body: String): List[(Urn, TrackAudioMetadata)] = {
      (Json.parse(body) \ "collection")
        .as[List[JsValue]]
        .map(json => {
          ((json \ "track_urn").as[Urn] -> (json).as[TrackAudioMetadata])
        })
    }

    inBatches(trackUrns.toList, batchSize) { urnBatch =>
      {
        val path = Path() / "tracks" / "audio" // okidoki does unwanted magic without the trailing "/"
        service.getWithSession(session, path / "", Params("urns" -> urnBatch.mkString(",")), Headers.empty()).map {
          response: Response =>
            response.status match {
              case Successful(_) => parseJson(response.contentString)
              case _ => List.empty
            }
        }
      }
    }.map(_.toMap)
  }

  def fetchTrackGeoblockings(
      session: UserSession,
      trackUrns: Set[Urn],
      batchSize: Int = 50
  ): Future[Map[Urn, Geoblockings]] = {
    def parseJson(body: String): List[(Urn, Geoblockings)] = {
      (Json.parse(body) \ "collection")
        .as[List[JsValue]]
        .map(json => {
          ((json \ "track_urn").as[Urn] -> (json \ "geo_blockings").asOpt[List[String]].getOrElse(List.empty))
        })
    }

    inBatches(trackUrns.toList, batchSize) { urnBatch =>
      {
        val path = Path() / "tracks" / "geo_blockings" / "" // okidoki does unwanted magic without the trailing "/"
        service.getWithSession(session, path, Params("urns" -> urnBatch.mkString(",")), Headers.empty()).map {
          response: Response =>
            response.status match {
              case Successful(_) => parseJson(response.contentString)
              case _ => List.empty
            }
        }
      }
    }.map(_.toMap)
  }

  def fetchUsersMap(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, User]] = {
    inBatches(urns.toList, batchSize) { urnBatch =>
      {
        service
          .getWithSession(
            session,
            Path() / "users" / "fetch",
            Params("urns" -> urnBatch.mkString(",")),
            Headers.empty()
          )
          .map { response: Response =>
            response.status match {
              case Successful(_) => Json.parse(response.contentString).as[List[JsValue]].map(UserMapper(_))
              case _ => List.empty
            }
          }
      }
    }.map(_.map(user => (user.urn -> user)).toMap)
  }
}
