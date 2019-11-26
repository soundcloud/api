package com.soundcloud.bff.nextbff.test

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.session.{AnonymousUserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}

class FakeUserAuthenticationSpec extends UnitSpecification {
  trait AnonymousUserContext extends Scope {
    val anonymousSession = (new UserSessionBuilder).build.asInstanceOf[AnonymousUserSession]
    val blankRequest = mock[HandlerRequest]

    lazy val subject = new FakeUserAuthentication(anonymousSession)
      .withLoggedInUser(blankRequest) { (session, urn) =>
        Future.value(ResponseBuilder.ok())
      }
    lazy val result =
      Await.result(subject)
  }

  "returns a 401 for an AnonymousUser" in new AnonymousUserContext {
    result.statusCode must be_==(401)
  }
}
