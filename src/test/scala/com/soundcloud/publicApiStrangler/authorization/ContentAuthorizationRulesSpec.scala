package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Urn, UserTier}
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

import scala.collection.JavaConversions._

class ContentAuthorizationRulesSpec extends UnitSpecification {

  trait Context extends Scope {
    val contentAuthMock = mock[ContentAuthorizationService]
    val subsServiceMock = mock[SubscriptionsService]
    val service = new ContentAuthorizationRules(contentAuthMock, subsServiceMock)
    val urns = Seq(Urn("soundcloud:tracks:123"), Urn("soundcloud:tracks:456"))
    val authorizations = urns.map(urn => new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE))

    def sessionWithTier(tier: UserTier): UserSession = {
      val session = loggedInSession(Urn("soundcloud:users:667"))
      tier match {
        case UserTier.HIGH => session.copyWithFeatures(Set("content_high_tier"))
        case UserTier.MID => session.copyWithFeatures(Set("content_mid_tier"))
        case _ => session
      }
    }
  }

  "does not lookup consumer subs country for free-tier users" in new Context {
    val session = sessionWithTier(UserTier.FREE)

    contentAuthMock.findRulesApplicableTo(session, urns, None) returns Future.value(authorizations.map(toBigJvmKitContentAuthorization))

    Await.result(service.fetchRules(session, urns)) ==== authorizations

    verifyZeroInteractions(subsServiceMock)
  }

  "looks up consumer subs country for high-tier subscriber" in new Context {
    val session = sessionWithTier(UserTier.HIGH)

    subsServiceMock.getActiveSubscriptionCountry(session) returns Future.value("US")
    contentAuthMock.findRulesApplicableTo(session, urns, Option("US")) returns Future.value(authorizations.map(toBigJvmKitContentAuthorization))

    Await.result(service.fetchRules(session, urns)) ==== authorizations
  }

  "looks up consumer subs country for mid-tier subscriber" in new Context {
    val session = sessionWithTier(UserTier.MID)

    subsServiceMock.getActiveSubscriptionCountry(session) returns Future.value("US")
    contentAuthMock.findRulesApplicableTo(session, urns, Option("US")) returns Future.value(authorizations.map(toBigJvmKitContentAuthorization))

    Await.result(service.fetchRules(session, urns)) ==== authorizations
  }
}
