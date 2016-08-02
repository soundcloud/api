package com.soundcloud.publicApiStrangler.test

import com.soundcloud.bff.finagle.ResponseLike
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.soundcloud.scalakit.UserSession
import com.twitter.util.Future

class FakePublicApiSiloing extends PublicApiSiloing(null, null, null) {
  override def withSiloedSession[T: ResponseLike](userSession: UserSession)(action: => Future[T]): Future[T]  = action
}
