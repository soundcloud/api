package com.soudcloud.authorization

import org.mockito.Mockito.verifyZeroInteractions

import com.soundcloud.bff.BazookaConfigComponent
import com.soundcloud.bff.BffController
import com.soundcloud.bff.authorization.AuthorizationRules
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.authorization.Policies
import com.soundcloud.bff.authorization.Policies._
import com.soundcloud.bff.finagle.{Request => BffRequest}
import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.bff.web.UserAuthenticationComponent
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.json.Json
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Await
import com.twitter.util.Future

import play.api.libs.json.JsObject

class AuthorizeContentSpec extends UnitSpecification with Fixtures {

  trait Context extends VerifiedMocks {
    val session = mock[UserSession]
    val contentAuthorization = mock[ContentAuthorizationService]
    val request = mock[BffRequest]
    val userAuthentication = new BazookaConfigComponent with BffController with UserAuthenticationComponent {
      override val applicationName = "test"
      override def withUserSession(request: BffRequest)(action: (UserSession) => Future[ResponseBuilder]) = {
        request mustEqual Context.this.request
        action(session)
      }
    }
    def content: String
    def status: Int

    val authorizeContent = new AuthorizeContent(contentAuthorization, userAuthentication)

    lazy val authorize = authorizeContent.apply(request, status, content)
    lazy val authorizedResponse = Await.result(authorize.map(_.build))
  }

  trait TrackContext extends Context {
    val content = singleTrack.toString
    val status = 200
    val urn = Urn("soundcloud:tracks:153896632")
    def policies: Policies

    override def before =
      when(contentAuthorization.findRulesApplicableTo(session, Seq(urn)))
        .thenReturn(Future(Seq(AuthorizationRules(urn, policies))))
  }

  "renders the track if authorized" in new TrackContext {
    lazy val policies = Policies(allowed, allowed)
    lazy val trackPolicies = new TrackPolicies(policies)
    lazy val authorizedTrackJson = trackPolicies.apply(session, singleTrack.as[JsObject])

    authorizedResponse.statusCode mustEqual 200
    authorizedResponse.getContentString mustEqual Json.stringify(authorizedTrackJson.get)
  }

  "renders not found if the track isn't authorized" in new TrackContext {
    lazy val policies = Policies(blocked, blocked)

    authorizedResponse.statusCode mustEqual 404
    authorizedResponse.getContentString mustEqual ""
  }

  trait NoTracksContext extends Context {
    lazy val content = "no tracks"
    lazy val status = 404
  }

  "doesn't authorize content if there aren't tracks to authorize" in new NoTracksContext {
    authorizedResponse.statusCode mustEqual status
    authorizedResponse.getContentString mustEqual content

    verifyZeroInteractions(contentAuthorization)
  }
}
