package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.bff.ratelimiting.internal.core.RateLimitClassifier
import com.soundcloud.jvmkit.module.bff.ratelimiting.internal.utilities.RegexExtensions._
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.publicApiStrangler.handler.SearchHandler._


object RateLimits {

  private val searchParams = defaultParams ++ trackParams ++ playlistParams
  private val searchZKBucket = "search"

  private def searchRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if searchParams.find(x => req.params.contains(x)).isDefined => true
  }

  val searchRateLimiter = new RateLimitClassifier(searchZKBucket, searchRequests)

  private val playsRegex = """(\/i1)?\/tracks\/(.+)\/stream.*""".r
  private val playsZKBucket = "plays"

  private def playRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if playsRegex =~ req.path => true
  }

  val playsRateLimiter = new RateLimitClassifier(playsZKBucket, playRequests)

}
