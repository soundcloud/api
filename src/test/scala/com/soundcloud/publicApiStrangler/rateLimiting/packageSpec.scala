package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification

class packageSpec extends UnitSpecification {
  "consumer" >> {
    "can be made from InetAddress" in {
      (Ip("1.1.2.3"): Consumer).identifier must be_==("1.1.2.3")
    }
  }
}
