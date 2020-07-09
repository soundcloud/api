package com.soundcloud.publicApiStrangler.handler.representation.tracks

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentation, TracksCollection}
import com.twitter.finagle.http.{Response, Status}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.twitter.util.Future
import play.api.libs.json.Json

object TrackRepresentationResponse {

  def handleTracksCollectionResponseFromService(
      outcome: Future[Outcome[TracksCollection]],
      hasLinkedPartitioning: Boolean
  ): Future[Response] = {
    outcome
      .map {
        case Good(tracksRepresentationResult) =>
          JsonResponseBuilder.ok(
            TracksCollection.getRepresentation(tracksRepresentationResult, hasLinkedPartitioning)
          )
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
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

  private def generateResponse(status: Status, rawContent: String): Response = {
    JsonResponseBuilder(status = status, body = rawContent).build
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))
}
