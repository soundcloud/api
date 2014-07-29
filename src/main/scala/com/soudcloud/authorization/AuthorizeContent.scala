package com.soudcloud.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json
import com.twitter.util.Future

import play.api.libs.json.JsValue

class AuthorizeContent(
  contentAuthorization: ContentAuthorizationService,
  userAuthentication: UserAuthenticationComponent) {

  def apply(request: BffRequest, status: Int, content: String): Future[ResponseBuilder] =
    CollectTrackUrns(content) match {
      case Some((json, urns)) => authorize(request, status, json, urns)
      case None => Future(render.body(content).status(status))
    }

  private def authorize(request: BffRequest, status: Int, content: JsValue, urns: List[Urn]) =
    userAuthentication.withUserSession(request) { session =>
      contentAuthorization.findRulesApplicableTo(session, urns).map { rules =>
        ApplyTrackPolicies(session, content, rules)
          .map(Json.stringify)
          .map(render.body(_).status(status))
          .getOrElse(render.notFound)
      }
    }

  private def render = new ResponseBuilder
}
