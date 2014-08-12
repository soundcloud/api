package com.soudcloud.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ Request => BffRequest }
import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future

class AuthorizeContent(
  contentAuthorization: ContentAuthorizationService,
  userAuthentication: UserAuthenticationComponent) {

  def apply(request: BffRequest, status: Int, body: String): Future[ResponseBuilder] =
    authorize(request, status, body, Response(body))

  private def authorize(request: BffRequest, status: Int, body: String, originalResponse: Response): Future[ResponseBuilder] =
    CollectTrackUrns(originalResponse.content) match {
      case Some((visitor, urns)) =>
        authorize(request, status, visitor, urns, originalResponse)
      case None =>
        Future(originalResponse.render.status(status))
    }

  private def authorize(request: BffRequest, status: Int, visitor: TracksVisitor, urns: List[Urn], originalResponse: Response) =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.findRulesApplicableTo(session, urns).map { rules =>
        ApplyTrackPolicies(session, visitor, rules)
          .map(StringifyContent(_))
          .map(originalResponse.withBody(_))
          .map(_.status(status))
          .getOrElse(render.notFound)
      }
    }

  private def render = new ResponseBuilder
}
