package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.{UserSessionBuilder, LoggedInUserSession, AnonymousUserSession, FailsafeUserSession}
import com.soundcloud.ratelimiting.core.ClientApplication
import com.soundcloud.scalakit.{AnonymousUserSession, Urn}
import org.specs2.mutable.Specification

class ActionableAccessMechanismSpec extends Specification {
  "ActionableAccessMechanism.fromSession" should {
    "return None if the session is failsafe" in {
      val session = new FailsafeUserSession(null)

      ActionableAccessMechanism.fromSession(session) mustEqual None
    }

    "return Anonymous when the user is null" in {
      val clientUrn = Urn("soundcloud", "applications", "derp")
      val anonymousSession = new UserSessionBuilder().setAgent(clientUrn).build()

      ActionableAccessMechanism.fromSession(anonymousSession) mustEqual
        Some(ActionableAccessMechanism.Anonymous(ClientApplication(clientUrn)))
    }

    "return LoggedIn when the user is not null" in {
      val user = Urn("soundcloud", "users", "herp")
      val clientUrn = Urn("soundcloud", "applications", "derp")
      val loggedInSession = new UserSessionBuilder().setUser(user).setAgent(clientUrn).build()

      ActionableAccessMechanism.fromSession(loggedInSession) mustEqual
        Some(ActionableAccessMechanism.LoggedIn(ClientApplication(clientUrn), user))
    }

    "return None if the session is failsafe" in {
      val session = new FailsafeUserSession(null)

      ActionableAccessMechanism.fromSession(session) mustEqual None
    }
  }
}
