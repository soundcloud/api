package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.config.InMemoryConfig
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Request, Status}
import com.twitter.util.{Await, Future}

class PublicApiSiloingSpec extends UnitSpecification {

  trait Context extends Scope {

    protected val request = mock[Request]

    protected def getPublicApiSiloing(blacklist: Set[Urn] = defaultMobileBlacklist) = {
      new PublicApiSiloing(() => Future.value(true), blacklist, new Telemetry((new InMemoryConfig).getApplicationName, MetricsRegistryImpl.defaultRegistry))
    }

    protected val soundCloudIOSApp = Urn("soundcloud", "applications", "124")
    protected val publicApp = Urn("soundcloud", "applications", "010101010")
    private val defaultMobileBlacklist = Set(Urn("soundcloud", "applications", "124"))

    protected def getUserSessionFor(appId: Urn) = new UserSessionBuilder().setAgent(appId).build()

    protected def action = Future.value(ResponseBuilder.ok())
  }

  "withSiloedUserSession" >> {
    "should return unauthorized for a token issued for a mobile app" in new Context {
      val mobileSession = getUserSessionFor(soundCloudIOSApp)
      val response = Await.result(getPublicApiSiloing().withSiloedSession(mobileSession)(action))

      response.status ==== Status.Unauthorized
    }

    "should successfully process a token issued for the public api" in new Context {
      val publicApiSession = getUserSessionFor(publicApp)
      val response = Await.result(getPublicApiSiloing().withSiloedSession(publicApiSession)(action))

      response.status ==== Status.Ok
    }
  }

  "public api siloing" >> {
    "should correctly parse blacklisted urls" in new Context {
      val urls = Set(Urn("soundcloud", "applications", "1"), Urn("soundcloud", "applications", "2"), Urn("soundcloud", "applications", "3"))

      val publicApiSiloing = getPublicApiSiloing(urls)

      val blacklistedSession1 = getUserSessionFor(Urn("soundcloud", "applications", "1"))
      val blacklistedResponse1 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession1)(action))
      blacklistedResponse1.status ==== Status.Unauthorized

      val blacklistedSession2 = getUserSessionFor(Urn("soundcloud", "applications", "2"))
      val blacklistedResponse2 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession2)(action))
      blacklistedResponse2.status ==== Status.Unauthorized

      val blacklistedSession3 = getUserSessionFor(Urn("soundcloud", "applications", "3"))
      val blacklistedResponse3 = Await.result(publicApiSiloing.withSiloedSession(blacklistedSession3)(action))
      blacklistedResponse3.status ==== Status.Unauthorized

      val correctSession = getUserSessionFor(Urn("soundcloud", "applications", "100"))
      val correctResponse = Await.result(publicApiSiloing.withSiloedSession(correctSession)(action))
      correctResponse.status ==== Status.Ok
    }
  }
}
