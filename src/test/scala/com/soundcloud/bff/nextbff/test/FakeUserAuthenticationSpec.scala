package com.soundcloud.bff.nextbff.test

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.{AnonymousUserSession, UserSessionBuilder}
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.{Await, Future}

class FakeUserAuthenticationSpec extends UnitSpecification {

  trait AnonymousUserContext extends VerifiedMocks {
    val anonymousSession = (new UserSessionBuilder)
      .build.asInstanceOf[AnonymousUserSession]
    val blankRequest = mock[Request]

    lazy val subject = new FakeUserAuthentication(anonymousSession)
      .withLoggedInUser(blankRequest) { (session, urn) =>
        Future.value(new ResponseBuilder().ok.build)
      }
    lazy val result = 
      Await.result(subject)
  }

  "returns a 401 for an AnonymousUser" in new AnonymousUserContext {
    result.statusCode must be_==(401)
  }

}
