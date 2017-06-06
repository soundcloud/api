package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Urn, UserTier}
import com.twitter.util.Future
import com.soundcloud.publicApiStrangler.authorization.policies._

class ContentAuthorizationRules(contentAuth: ContentAuthorizationService, subscriptions: SubscriptionsService) {

  def fetchRules(session: UserSession, urns: Seq[Urn]): Future[Seq[ContentAuthorization]] =
    consumerSubsCountry(session).flatMap(country => contentAuth.findRulesApplicableTo(session, urns, country))

  private def consumerSubsCountry(session: UserSession): Future[Option[String]] =
    if (session.getTier != UserTier.FREE) {
      subscriptions.getActiveSubscriptionCountry(session).map(Option(_))
    } else {
      Future.None
    }


}
