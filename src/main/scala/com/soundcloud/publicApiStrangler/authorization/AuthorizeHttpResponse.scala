package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ Request => BffRequest }
import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future
import com.soundcloud.scalakit.json.Json

class AuthorizeHttpResponse(
  contentAuthorization: ContentAuthorizationService,
  userAuthentication: UserAuthenticationComponent) {

  def apply(request: BffRequest, status: Int, body: String): Future[ResponseBuilder] =
    authorize(request, status, body, BuilderResponse(body))

  private def authorize(request: BffRequest, status: Int, body: String, originalResponse: BuilderResponse): Future[ResponseBuilder] =
    CollectTrackUrns(originalResponse.content) match {
      case Some((visitor, urns)) =>
        authorize(request, status, visitor, urns, originalResponse)
      case None =>
        Future(originalResponse.render.status(status))
    }

  private def authorize(request: BffRequest, status: Int, visitor: TracksVisitor, urns: List[Urn], originalResponse: BuilderResponse) =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.findRulesApplicableTo(session, urns).map { rules =>
        ApplyTrackPolicies(session, visitor, rules)
          .map(Json.stringify)
          .map(originalResponse.withBody)
          .map(_.status(status))
          .getOrElse(render.forbidden)
      }
    }

  private def render = new ResponseBuilder
}
