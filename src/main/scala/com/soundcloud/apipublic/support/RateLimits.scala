package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.bff.ratelimiting.RateLimitClassifier
import com.soundcloud.jvmkit.module.http.server.HandlerRequest

object RateLimits {
  private val playsRegex = """(\/i1)?\/tracks\/(.+)\/stream.*""".r
  private val playsZKBucket = "plays"

  private def playRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if playsRegex.findFirstMatchIn(req.path).isDefined => true
  }

  val playsRateLimiter = new RateLimitClassifier(playsZKBucket, playRequests)
}
