package com.soundcloud.publicApiStrangler.handler.representation.tracks

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.CreatedTrack.CreatedTrack
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

object TrackRepresentationResponse {

  def handleCreateTrackResponseFromService(
      outcome: Future[Outcome[CreatedTrack]]
  ): Future[Response] = {
    outcome.map {
      case Good(createdTrack) => {
        val headers = Map("Location" -> createdTrack.location)
        generateResponse(Status.Created, Json.stringify(Json.toJson(createdTrack)), headers)
      }
      case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
      case Bad(NotValid(_)) => JsonResponseBuilder.badRequest(generateErrorBody("invalid request"))

      case _ => throw new UnhandledOutcomeException
    }
  }

  def handleTrackRepresentationResponseFromService(
      outcome: Future[Outcome[TrackRepresentation]]
  ): Future[Response] = {
    outcome.map {
      case Good(trackRep) =>
        generateResponse(Status.Ok, Json.stringify(Json.toJson(trackRep)))
      case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
      case Bad(NotValid(_)) => JsonResponseBuilder.badRequest(generateErrorBody("invalid request"))

      case _ => throw new UnhandledOutcomeException
    }
  }

  private def generateResponse(
      status: Status,
      rawContent: String,
      headers: Map[String, String] = Map.empty
  ): Response = {
    JsonResponseBuilder(status = status, body = rawContent, headers = headers).build
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))
}
