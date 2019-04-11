package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Urn, UserTier}
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}

import scala.collection.JavaConverters._

class ContentAuthorizationRulesSpec extends UnitSpecification {

  trait Context extends Scope {
    val contentAuthMock = mock[ContentAuthorizationService]
    val subsServiceMock = mock[SubscriptionsService]
    val service = new ContentAuthorizationRules(contentAuthMock, subsServiceMock)
    val urns = Seq(Urn("soundcloud", "tracks", "123"), Urn("soundcloud", "tracks", "456"))
    val authorizations = urns.map(urn => new ContentAuthorization(urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE))

    def sessionWithTier(tier: UserTier): UserSession = {
      val session = loggedInSession(Urn("soundcloud", "users", "667"))
      tier match {
        case UserTier.HIGH => session.copyWithFeatures(Set("content_high_tier").asJava)
        case UserTier.MID => session.copyWithFeatures(Set("content_mid_tier").asJava)
        case _ => session
      }
    }
  }

  "looks up geo country for free-tier users" in new Context {
    val session = sessionWithTier(UserTier.FREE)

    contentAuthMock.findRulesApplicableTo(session, urns, Some("US")) returns Future.value(authorizations)

    Await.result(service.fetchRules(session, urns)) ==== authorizations

    there were noCallsTo(subsServiceMock)
  }

  "looks up consumer subs country for high-tier subscriber" in new Context {
    val session = sessionWithTier(UserTier.HIGH)

    subsServiceMock.getActiveSubscriptionCountry(session) returns Future.value(Option("FR"))
    contentAuthMock.findRulesApplicableTo(session, urns, Option("FR")) returns Future.value(authorizations)

    Await.result(service.fetchRules(session, urns)) ==== authorizations
  }

  "looks up consumer subs country for mid-tier subscriber" in new Context {
    val session = sessionWithTier(UserTier.MID)

    subsServiceMock.getActiveSubscriptionCountry(session) returns Future.value(Option("FR"))
    contentAuthMock.findRulesApplicableTo(session, urns, Option("FR")) returns Future.value(authorizations)

    Await.result(service.fetchRules(session, urns)) ==== authorizations
  }

  "looks up geo country when subscription country is unavailable" in new Context {
    val session = sessionWithTier(UserTier.MID)

    subsServiceMock.getActiveSubscriptionCountry(session) returns Future.value(None)
    contentAuthMock.findRulesApplicableTo(session, urns, Option("US")) returns Future.value(authorizations)

    Await.result(service.fetchRules(session, urns)) ==== authorizations
  }
}
