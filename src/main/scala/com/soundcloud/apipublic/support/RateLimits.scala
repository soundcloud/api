package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.bff.ratelimiting.RateLimitClassifier
import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.apipublic.handler.search.SearchHandler._
import com.soundcloud.apipublic.support.oauth.ClientCredentialsGrant
import com.twitter.finagle.http.Method

object RateLimits {
  private val searchParams = SearchRateLimits.defaultParams ++ SearchRateLimits.trackParams ++ SearchRateLimits.playlistParams
  private val searchZKBucket = "search"

  private def searchRequests: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if searchParams.exists(x => req.params.contains(x)) => true
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

  private val oauthGrantExchangePath = "/oauth2/token"
  private val clientCredentialsExchangeBucket = "client-credentials-exchange"
  private def clientCredentialsExchangeRequest: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if matchesClientCredentialsExchangeRequest(req) => true
  }

  private def matchesClientCredentialsExchangeRequest(req: HandlerRequest): Boolean = {
    (req.method == Method.Post) &&
    req.path == oauthGrantExchangePath &&
    req.params.get("grant_type").contains(ClientCredentialsGrant.Name)
  }

  val clientCredentialsExchangeRateLimiter =
    new RateLimitClassifier(clientCredentialsExchangeBucket, clientCredentialsExchangeRequest)

  val dummyRateLimiter = new RateLimitClassifier(dummyBucket, dummyRequests)

  private val nonClientCredentialsExchangeBucket = "non-client-credentials-exchange"

  private def nonClientCredentialsExchangeRequest: RateLimitClassifier.rateLimitClassifier = {
    case req: HandlerRequest if matchesClientCredentialsExchangeRequest(req) => false
  }

  val nonClientCredentialsExchangeRateLimiter =
    new RateLimitClassifier(nonClientCredentialsExchangeBucket, nonClientCredentialsExchangeRequest)

}
