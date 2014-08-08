package com.soudcloud.authorization

import com.soudcloud.data.ParsedValue
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ResponseBuilder, Request => BffRequest}
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future

class AuthorizeContent(
  contentAuthorization: ContentAuthorizationService,
  userAuthentication: UserAuthenticationComponent) {

  def apply(request: BffRequest, status: Int, content: String): Future[ResponseBuilder] =
    CollectTrackUrns(content) match {
      case Some((data, urns)) => authorize(request, status, data, urns)
      case None => Future(render.body(content).status(status))
    }

  private def authorize(request: BffRequest, status: Int, content: ParsedValue, urns: List[Urn]) =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.findRulesApplicableTo(session, urns).map { rules =>
        ApplyTrackPolicies(session, content, rules)
          .map(_.stringify)
          .map(render.body(_).status(status))
          .getOrElse(render.notFound)
      }
    }

  private def render = new ResponseBuilder
}
