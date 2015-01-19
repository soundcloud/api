package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.UserSession
import com.twitter.util.Future

/**
 * This is only temporary here till we have the real client implementation available in sc-services.
 */
class GateKeeperClient {

  def isFeatureAccessible(session: UserSession, featureName: String): Future[Boolean] = Future.value(false)

}
