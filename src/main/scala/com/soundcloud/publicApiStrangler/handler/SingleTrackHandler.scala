package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentationsService
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Try}
import play.api.libs.json._
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TrackRepresentation
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.UnhandledOutcomeException

class SingleTrackHandler(
    userAuthentication: UserAuthentication,
    tracksService: TrackRepresentationsService,
    telemetry: Telemetry
) {

  def renderTrack(req: HandlerRequest): Future[Response] = {
    stripConditionalRequestHeaders(req)

    userAuthentication.withUserSession(req) { session =>
      Try(trackUrn(req)) match {
        case Return(urn) =>
          val secretToken = req.params.get("secret_token")
          fetchTrackRepresentation(session, urn, secretToken)
            .map {
              case Good(trackRep) => generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
              case Bad(NotFound(_)) => generateNotFound
              case _ => throw new UnhandledOutcomeException
            }
        case _ => Future.value(generateNotFound)
      }
    }
  }

  private def fetchTrackRepresentation(
      session: UserSession,
      urn: Urn,
      secretToken: Option[String]
  ): Future[Outcome[TrackRepresentation]] = {
    tracksService
      .track(session, TrackRequest(urn, secretToken))
      .map {
        case Some(track) => Good(track)
        case _ => NotFound().bad
      }
  }

  private def generateNotFound: Response = {
    JsonResponseBuilder.notFound(notFoundErrorString)
  }

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private val notFoundErrorString = """{"errors":[{"error_message":"404 - Not Found"}]}"""

  /*
   * If-None-Match header causes mothership to return 304
   * We decided not to support this behavior
   */
  private def stripConditionalRequestHeaders(req: HandlerRequest): Option[String] = {
    req.headerMap.remove("If-None-Match")
  }

}
