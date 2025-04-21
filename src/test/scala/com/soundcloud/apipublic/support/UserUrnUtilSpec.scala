package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.support.UserUrnUtil.getUserUrn
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class UserUrnUtilSpec extends Specification with Mockito {
  "yields user urn for request containing a valid user numeric id" in new Scope {
    getUserUrn("1234") ==== Urn("soundcloud", "users", "1234")
  }

  "yields user urn for request containing a valid user urn" in new Scope {
    getUserUrn("abc1234") ==== Urn("soundcloud", "users", "abc1234")
  }
}
