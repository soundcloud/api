package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.UntypedJson
import com.soundcloud.bff.nextbff.pagination.PageBuilder
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.filter.DefaultResponseHeaders
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSoundsMapper, SimilarSoundsMapping}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

/**
  * Overrides the public api endpoint used to retrieve similar tracks.
  */
class SimilarSoundsHandler(
                            userAuthentication: UserAuthentication,
                            similarSoundsMapper: SimilarSoundsMapper,
                            baseUrl: String
                          ) {

  def handleSimilarSoundsRequest(request: HandlerRequest): Future[Response] = {
    userAuthentication.withUserSession(request) {
      (session: UserSession) =>
        val trackUrn = new Urn("soundcloud", "tracks", request.routeParams("trackId"))

        val page = PageBuilder(request, baseUrl)(trackUrn).
          allowExtraParams(Set(SimilarSoundsMapping.LinkedPartitioning)).
          defaultLimit(50).
          buildOffsetBased(0)

        similarSoundsMapper.materialize(session, page).map {
          case Some(info: SimilarSoundsMapping) => JsonResponseBuilder(body = UntypedJson.write(if (shouldPaginate(request.params)) info else info.collection)).build
          case None => ResponseBuilder.notFound()
        }.map(enrichWithDefaultHeaders)
    }
  }

  private def enrichWithDefaultHeaders(response: Response): Response = {
    DefaultResponseHeaders.defaultHeaders.foreach {
      case (key, value) => response.headerMap.set(key, value)
    }
    response
  }

  private def shouldPaginate(params: Params) = {
    params
      .get(SimilarSoundsMapping.LinkedPartitioning)
      .flatMap(_.value.headOption)
      .exists(_.nonEmpty)
  }

}
