package com.soundcloud.publicApiStrangler.handler.representation.collection

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.{Json, Writes}

object CollectionResponse {
  def handleCollectionResponse[T: Writes](
      outcome: Future[Outcome[Collection[T]]],
      hasLinkedPartitioning: Boolean
  ): Future[Response] = {
    outcome
      .map {
        case Good(collectionResponse) =>
          JsonResponseBuilder.ok(
            Collection.getRepresentation(collectionResponse, hasLinkedPartitioning)
          )

        case Bad(NotValid(_)) => JsonResponseBuilder.badRequest(generateErrorBody("invalid request"))
        case Bad(NotFound(_)) => JsonResponseBuilder.notFound(generateErrorBody("not found"))
        case _ => throw new UnhandledOutcomeException
      }
  }

  private def generateErrorBody(message: String): String =
    Json.stringify(Json.obj("error" -> message))
}
