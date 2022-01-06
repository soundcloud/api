package com.soundcloud.apipublic.client.mothership

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.json.play.UrnFormat._
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.apipublic.client.mothership.response.representation.{Geoblockings, UserRepresentation}
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.Successful
import com.twitter.util.Future
import play.api.libs.json._

sealed trait OkidokiError
object UnprocessableEntity extends OkidokiError
object TooManyRequests extends OkidokiError
case class RateLimitedError(spamWarningUrn: Urn) extends OkidokiError

class RichOkidokiClient(service: JsonClient, exceptionCollector: ExceptionCollector)
    extends OkidokiClient(service, exceptionCollector) {

  def fetchTrackGeoblockings(
      session: UserSession,
      trackUrns: Set[Urn],
      batchSize: Int = 50
  ): Future[Map[Urn, Geoblockings]] = {
    def parseJson(body: String): List[(Urn, Geoblockings)] = {
      (Json.parse(body) \ "collection")
        .as[List[JsValue]]
        .map(json => {
          (json \ "track_urn").as[Urn] -> (json \ "geo_blockings").asOpt[List[String]].getOrElse(List.empty)
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

  def fetchUsersMap(session: UserSession, urns: Set[Urn], batchSize: Int = 50): Future[Map[Urn, UserRepresentation]] = {
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
              case Successful(_) =>
                Json.parse(response.contentString).as[List[JsValue]].map(UserRepresentationMapper(_))
              case _ => List.empty
            }
          }

      }
    }.map(_.map(user => (user.urn -> user)).toMap)
  }
}
