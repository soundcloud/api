package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.support.UserUrnUtil.getUserUrn
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class UserUrnUtilSpec extends Specification with Mockito {
  "yields user urn for request containing a valid user id" in new Scope {
    getUserUrn("1234") ==== Urn("soundcloud", "users", "1234")
  }

  "throws illegal state exception if not a valid user id" in new Scope {
    getUserUrn("abc1234") must throwA[IllegalArgumentException]
  }
}
