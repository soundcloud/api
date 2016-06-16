package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.Request
import com.soundcloud.publicApiStrangler.controller.SearchController._
import com.soundcloud.ratelimiting.internal.core.RateLimitClassifier
import com.soundcloud.ratelimiting.internal.utilities.RegexExtensions._

object RateLimits {

  private val searchParams = defaultParams ++ trackParams ++ playlistParams
  private val searchZKBucket = "search"

  private def searchRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: Request if searchParams.find(x => req.params.contains(x)).isDefined => true
  }

  val searchRateLimiter = new RateLimitClassifier(searchZKBucket, searchRequests)

  private val playsRegex = """(\/i1)?\/tracks\/(.+)\/stream.*""".r
  private val playsZKBucket = "plays"

  private def playRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: Request if playsRegex =~ req.path => true
  }

  val playsRateLimiter = new RateLimitClassifier(playsZKBucket, playRequests)

}
