package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.error.UnhandledOutcomeException
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.apipublic.support.ErrorResponse
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.Response
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
              case Good(trackRep) => JsonResponseBuilder.ok(Json.stringify(Json.toJson(trackRep)))
              case Bad(NotFound(_)) => ErrorResponse.notFound()
              case _ => throw new UnhandledOutcomeException
            }
        case Throw(e) => Future.value(ErrorResponse.badRequest(e.getMessage))
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
}
