package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.jvmkit.{Urn, UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}

class PublicApiSiloingSpec extends UnitSpecification {
  trait Context extends VerifiedMocks{

    protected val request = mock[Request]
    private val rollout = mock[Rollout]

    protected def getPublicApiSiloing(blacklist: Set[Urn] = defaultMobileBlacklist) = {
      new PublicApiSiloing(()=> Future.value(true), blacklist, new Telemetry(new InMemoryConfig))
    }

    protected val soundCloudIOSApp = new Urn("soundcloud:applications:124")
    protected val publicApp = new Urn("soundcloud:applications:010101010")
    private val defaultMobileBlacklist = Set(new Urn("soundcloud:applications:124"))

    protected def getUserSessionFor(appId: Urn) = new UserSessionBuilder().setAgent(appId).build()

    protected def action = Future.value(new ResponseBuilder().ok)
  }

  "withSiloedUserSession" >> {
    "should return unauthorized for a token issued for a mobile app" in new Context {
      val mobileSession = getUserSessionFor(soundCloudIOSApp)
      val response = Await.result(getPublicApiSiloing().withSiloedSession(mobileSession)(action))

      response.build.getStatusCode() ==== Status.Unauthorized.code
    }

    "should successfully process a token issued for the public api" in new Context {
      val publicApiSession = getUserSessionFor(publicApp)
      val response = Await.result(getPublicApiSiloing().withSiloedSession(publicApiSession)(action))

      response.build.getStatusCode() ==== Status.Ok.code
    }
  }

  "public api siloing" >> {
    "should correctly parse blacklisted urls" in new Context {
      val urls = Set(new Urn("soundcloud:applications:1"), new Urn("soundcloud:applications:2"), new Urn("soundcloud:applications:3"))

      val publicApiSiloing = getPublicApiSiloing(urls)

      val blacklistedSession1 = getUserSessionFor(new Urn("soundcloud:applications:1"))
      val blacklistedResponse1 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession1)(action))
      blacklistedResponse1.build.getStatusCode() ==== Status.Unauthorized.code

      val blacklistedSession2 = getUserSessionFor(new Urn("soundcloud:applications:2"))
      val blacklistedResponse2 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession2)(action))
      blacklistedResponse2.build.getStatusCode() ==== Status.Unauthorized.code

      val blacklistedSession3 = getUserSessionFor(new Urn("soundcloud:applications:3"))
      val blacklistedResponse3 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession3)(action))
      blacklistedResponse3.build.getStatusCode() ==== Status.Unauthorized.code

      val correctSession = getUserSessionFor(new Urn("soundcloud:applications:100"))
      val correctResponse = Await.result(publicApiSiloing.withSiloedSession(correctSession)(action))
      correctResponse.build.getStatusCode() ==== Status.Ok.code
    }
  }
}
