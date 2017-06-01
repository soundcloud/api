package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.{NotFound, Success}
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationsService
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json.Json

import scala.util.control.NonFatal


class SingleTrackHandler(userAuthentication: UserAuthentication, tracksService: TrackRepresentationsService, telemetry: Telemetry) {
  val logger = SoundCloudLoggerFactory.getLogger(getClass)

  private val numericRegexp = """\d+""".r

  def renderTrack(req: HandlerRequest): Future[Response] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { case session =>
      val trackId = req.routeParams("trackId")
      val callback = req.params.get("callback")

      Try(new Urn("soundcloud", "tracks", trackId)) match {
        case Return(urn@Urn(_, _, numericRegexp())) => {
          val secretToken = req.params.get("secret_token")
          tracksService.track(session, urn, secretToken).map {
            case Success(trackRep) => generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)), callback)
            case NotFound => generateNotFound(callback)
            case _ => {
              logger.error(s"Something went wrong while trying to fetch $urn")
              generateResponse(Status.InternalServerError, "Something went wrong while fetching a track", callback)
            }
          } handle {
            case NonFatal(e) => {
              logger.error(e.getMessage)
              generateResponse(Status.InternalServerError, "An unexpected error occured while fetching a track", callback)
            }
          }
        }
        case _ => Future.value(generateNotFound(callback))
      }
    }
  }

  private def generateNotFound(callback: Option[String]): Response = {
    JsonResponseBuilder.notFound(jsonpWrapper(callback, notFoundErrorString))
  }

  private def generateResponse(status: Status, rawContent: String, callback: Option[String]): Response = {
    JsonResponseBuilder(status = status, body = jsonpWrapper(callback, rawContent)).build
  }

  /**
    * This JsonpWrapper logic should go to filter,
    * but should be applied only to the migrated endpoitns.
    */
  private def jsonpWrapper(callback: Option[String], contentString: String): String =
    callback.map(cb => s"/**/$cb($contentString);").getOrElse(contentString)

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  /*
  * If-None-Match header causes mothership to return 304
  * We decided not to support this behavior
  */
  private def stripConditionalRequestHeaders(req: HandlerRequest): Option[String] = {
    req.headerMap.remove("If-None-Match")
  }
}
