package com.soundcloud.publicApiStrangler.test

import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.ResponseLike
import com.soundcloud.publicApiStrangler.authorization.PublicApiSiloing
import com.twitter.util.Future

class FakePublicApiSiloing extends PublicApiSiloing(null, null, null) {
  override def withSiloedSession[T: ResponseLike](userSession: UserSession)(action: => Future[T]): Future[T] = action
}
