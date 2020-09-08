package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.bff.ratelimiting.internal.core.RateLimitClassifier
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
    case req: HandlerRequest if playsRegex.findFirstMatchIn(req.path).isDefined => true
  }

  val playsRateLimiter = new RateLimitClassifier(playsZKBucket, playRequests)

  private val repostsRegex = """\/e1\/me\/(track|playlist)_reposts\/(.+)""".r
  private val repostsBucket = "reposts"

  private def repostRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if repostsRegex.findFirstMatchIn(req.path).isDefined => true
  }

  val repostsRateLimiter = new RateLimitClassifier(repostsBucket, repostRequests)

  private val dummyBucket = "dummy"

  private def dummyRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if req.path.startsWith("/dummy") => true
  }

  val dummyRateLimiter = new RateLimitClassifier(dummyBucket, dummyRequests)
}
