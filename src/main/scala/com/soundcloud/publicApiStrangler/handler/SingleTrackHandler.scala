package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.getTrackUrn
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json._

class SingleTrackHandler(
    userAuthentication: UserAuthentication,
    tracksService: TrackRepresentationsService
) {

  def renderTrack(req: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(req) { session =>
      Try(getTrackUrn(req)) match {
        case Return(urn) =>
          val secretToken = req.params.get("secret_token")
          fetchTrackRepresentation(session, urn, secretToken)
            .map {
              case Good(trackRep) => generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
              case Bad(NotFound(_)) => generateNotFound
              case _ => throw new UnhandledOutcomeException
            }
        case Throw(e) => Future.value(JsonResponseBuilder.badRequest(e.getMessage))
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

}
