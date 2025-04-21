package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.support.TrackUrnUtil.getTrackUrn
import com.twitter.finagle.http.ParamMap
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class TrackUrnUtilSpec extends Specification with Mockito {
  "yields track urn for request containing a valid urn param" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("trackId" -> "1234")

    getTrackUrn(request) ==== Urn("soundcloud", "tracks", "1234")
  }

  "yields track urn for request containing a id param" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("trackId" -> "abc1234")

    getTrackUrn(request) ==== Urn("soundcloud", "tracks", "abc1234")
  }
}
