package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, SinatraPathPatternParser}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.TrackUrnUtil.trackUrn
import com.twitter.finagle.http.{Method, Request}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class TrackUrnUtilSpec extends Specification {
  "yields track urn for request containing a valid routeparam" in new Scope {
    trackUrn(HandlerRequest(SinatraPathPatternParser("/a/:trackId"), Request(Method.Get, "/a/1234"))) ====
      Urn("soundcloud", "tracks", "1234")
  }

  "throws illegal state exception if not a valid track id" in new Scope {
    trackUrn(HandlerRequest(SinatraPathPatternParser("/a/:trackId"), Request(Method.Get, "/a/abc1234"))) must
      throwA[IllegalArgumentException]
  }
}
