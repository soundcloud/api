package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf
import org.mockito.Mockito.{times, verify, verifyZeroInteractions}
import org.mockito.{ArgumentCaptor, Mockito}

class ContentAuthorizationServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val authsy = mock[JsonClient]
    val contentAuthorizationService = new ContentAuthorizationService(authsy)
    val userSession = new UserSessionBuilder().build()
    val authsyPath = Path() / "tracks" / "authorization"
  }

  "fetch all rules for a given user session and resource list" >> {
    "returns rules for each element" in new Context {
      val resources = Seq(Urn("soundcloud", "tracks", "123"), Urn("soundcloud", "tracks", "666"), Urn("soundcloud", "tracks", "999"))

      authsy.getWithSession(userSession, authsyPath, Params("urns" -> resources), Headers.empty) returns Future(jsonResponse(Status.Ok, withContentsOf("authsy", "by_urns")))

      val rules = Await.result(contentAuthorizationService.findRulesApplicableTo(userSession, resources, None))
      rules ==== Seq(
        new ContentAuthorization(Urn("soundcloud", "tracks", "666"), ContentPolicy.from("monetize"), Reason.GEO, MonetizationModel.AD_SUPPORTED),
        new ContentAuthorization(Urn("soundcloud", "tracks", "123"), ContentPolicy.from("monetize"), Reason.GEO, MonetizationModel.AD_SUPPORTED),
        new ContentAuthorization(Urn("soundcloud", "tracks", "999"), ContentPolicy.from("allowed"), Reason.UNKNOWN, ContentRestriction.NO_OFFLINE_SYNC, MonetizationModel.NOT_APPLICABLE)
      )
    }
  }

  "sends a session with the subscription country header when subscription country is provided" in new Context {
    val resources = Seq(Urn("soundcloud", "tracks", "123"), Urn("soundcloud", "tracks", "666"), Urn("soundcloud", "tracks", "999"))

    authsy.getWithSession(any[UserSession], any[Path], any[Params], any[Headers]) returns Future(jsonResponse(Status.Ok, withContentsOf("authsy", "by_urns")))

    Await.result(contentAuthorizationService.findRulesApplicableTo(userSession, resources, Option("US")))

    val captor = ArgumentCaptor.forClass(classOf[UserSession])
    verify(authsy).getWithSession(captor.capture(), any[Path], any[Params], any[Headers])

    val authsySession = captor.getValue
    authsySession.getExtraHeaders.get(UserSession.CONSUMER_SUBSCRIPTION_COUNTRY) ==== "US"
  }

  "don't fetch if the resources sequence is empty" in new Context {
    Await.result(contentAuthorizationService.findRulesApplicableTo(userSession, Seq(), None)) ==== Seq()
    verifyZeroInteractions(authsy)
  }

  "when asking for more than 65 things, make several requests" in new Context {
    authsy.getWithSession(any[UserSession], any[Path], any[Params], any[Headers]) returns Future(jsonResponse(Status.Ok, withContentsOf("authsy", "by_urns")))

    Await.result(contentAuthorizationService.findRulesApplicableTo(userSession, List.fill(100)(Urn("soundcloud", "tracks", "123")), None))
    verify(authsy, times(2))
  }
}
