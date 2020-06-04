package com.soundcloud.publicApiStrangler.handler.representation.tracks

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.handler.HttpError
import com.soundcloud.publicApiStrangler.handler.support.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.trackrepresentation.TracksCollection
import com.soundcloud.publicApiStrangler.support.{Bad, Good, Result}
import com.twitter.finagle.http.{Response}
import com.twitter.util.Future
import play.api.libs.json.Json

object TrackRepresentationResponse {

  def handleResponseFromService(
      result: Future[Result[TracksCollection]],
      hasLinkedPartitioning: Boolean
  ): Future[Response] = {
    result
      .map {
        case Good(tracksRepresentationResult) =>
          JsonResponseBuilder.ok(
            TracksCollection.getRepresentation(tracksRepresentationResult, hasLinkedPartitioning)
          )
        case Bad(error: HttpError) =>
          JsonResponseBuilder(error.status, generateErrorBody(error.description)).build
        case _ =>
          throw new UnhandledOutcomeException
      }
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))

}
