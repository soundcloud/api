package com.soundcloud.publicApiStrangler.handler.representation.collection

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.support.ErrorResponse
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Writes

object CollectionResponse {
  val MaxCacheAge = 60

  def handleCollectionResponse[T: Writes](
      outcome: Future[Outcome[Collection[T]]],
      hasLinkedPartitioning: Boolean
  ): Future[Response] = {
    outcome
      .map {
        case Good(collectionResponse) =>
          val response = JsonResponseBuilder.ok(
            Collection.getRepresentation(collectionResponse, hasLinkedPartitioning)
          )
          appendCacheHeaders(response)

        case Bad(NotValid(_)) => ErrorResponse.badRequest()
        case Bad(NotFound(_)) => ErrorResponse.notFound()
        case _ => throw new UnhandledOutcomeException
      }
  }

  private def appendCacheHeaders(response: Response) = {
    response.headerMap.set("Cache-Control", s"public, max-age=$MaxCacheAge, must-revalidate")
    response
  }
}
