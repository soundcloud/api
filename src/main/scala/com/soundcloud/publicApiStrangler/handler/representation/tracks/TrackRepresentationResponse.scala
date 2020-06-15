package com.soundcloud.publicApiStrangler.handler.representation.tracks

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.handler.support.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TracksCollection
import com.twitter.finagle.http.Response
import com.soundcloud.outcome._
import com.twitter.util.Future
import play.api.libs.json.Json

object TrackRepresentationResponse {

  def handleResponseFromService(
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

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

}
