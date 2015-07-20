package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.bff.web.{BffInjectionBasedController, UserAuthentication}
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeaders
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapping.similarsounds.SimilarSoundsMapping
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.finagle.jsonservice.Params
import com.twitter.util.Future

/**
 * Overrides the public api endpoint used to retrieve similar tracks.
 */
class SimilarSoundsController(
                              userAuthentication: UserAuthentication,
                              similarSoundsMapper: SimilarSoundsMapper,
                              baseUrl: String
                             )
  extends BffInjectionBasedController {

  get("/tracks/:trackId/related")(handleSimilarSoundsRequest(_, similarSoundsMapper))
  get("/tracks/:trackId/related.json")(handleSimilarSoundsRequest(_, similarSoundsMapper))

  private def handleSimilarSoundsRequest(request: Request, mapper: SimilarSoundsMapper): Future[ResponseBuilder] = {
      userAuthentication.withUserSession(request) {
        (session: UserSession) =>
          val trackUrn = new Urn("soundcloud", "tracks", request.routeParams("trackId"))

          val page = PageBuilder(request, baseUrl)(trackUrn).
            allowExtraParams(Set(SimilarSoundsMapping.LinkedPartitioning)).
            defaultLimit(50).
            buildOffsetBased(0)

          mapper.materialize(session, page).map {
            case Some(info) => render.json(if (shouldPaginate(request.params)) info else info.collection)
            case None => render.notFound
          }.map(_.headers(DefaultResponseHeaders.defaultHeaders))
      }
  }

  private def shouldPaginate(params: Params) = {
    params
      .get(SimilarSoundsMapping.LinkedPartitioning)
      .flatMap(_.value.headOption)
      .exists(_.nonEmpty)
  }

}
