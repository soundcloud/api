package com.soudcloud.authorization

import com.soundcloud.scalakit.UTF8
import java.nio.charset.Charset

import scala.collection.JavaConversions.iterableAsScalaIterable

import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.rollout.RolloutRepository
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class ContentAuthorizationFilter(
  authorizeContent: AuthorizeContent,
  rolloutRepository: RolloutRepository)
  extends SimpleFilter[HandlerRequest, Response] {

  private val featureFlag = "PUBLIC_API_STRANGLER_CONTENT_AUTHORIZATION"

  override def apply(request: HandlerRequest, next: Service[HandlerRequest, Response]) =
    next(request).flatMap { response =>
      rolloutRepository.activated(request.userSession, featureFlag).flatMap {
        case true =>
          authorize(request, response)
        case false =>
          Future(response)
      }
    }

  private def authorize(request: HandlerRequest, response: Response) =
    authorizeContent(new BffRequest(request.request), response.statusCode, body(response)).map { render =>
      render.headers(headersMap(response)).build
    }

  private def body(response: Response) =
    response.getContent.toString(UTF8)

  private def headersMap(response: Response) =
    response.headers.map(e => e.getKey -> e.getValue).toMap
}
