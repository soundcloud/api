package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.support.TrackUrnUtil.trackUrn
import com.twitter.finagle.http.ParamMap
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class TrackUrnUtilSpec extends Specification with Mockito {
  "yields track urn for request containing a valid routeparam" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("trackId" -> "1234")

    trackUrn(request) ==== Urn("soundcloud", "tracks", "1234")
  }

  "throws illegal state exception if not a valid track id" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("trackId" -> "abc1234")

    trackUrn(request) must throwA[IllegalArgumentException]
  }
}
