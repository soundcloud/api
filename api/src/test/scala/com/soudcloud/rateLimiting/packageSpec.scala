package com.soudcloud.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import java.net.ServerSocket

class packageSpec extends UnitSpecification {
  "consumer" >> {
    "can be made from InetAddress" in {
      (new ServerSocket(12340).getInetAddress:Consumer).identifier must be_==("0.0.0.0")
    }
  }
}
