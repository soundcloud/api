package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.util.{Time, Duration, Future}

case class ApiClient(urn: Urn) {
  def identifier: String = urn.getIdentifier
}

case class ClientSpecificRateLimit(apiClient: ApiClient, rateLimit: RateLimit, applicationName: ResourceName) {
  lazy val prefix = s"${applicationName.getName}.rateLimit.${apiClient.identifier}.${rateLimit.identifier}"
  lazy val counterKey = s"$prefix.counter"
  lazy val expiryKey = s"$prefix.expiry"
}

sealed trait RateLimit {
  def ttl: Duration
  def maximumNrOfRequests: Int
  def identifier: String
}

case class GeneralRateLimit(ttl: Duration, maximumNrOfRequests: Int) extends RateLimit {
  def identifier: String = s"$maximumNrOfRequests/${ttl.inSeconds}seconds"
}

trait RateLimiter {
  def exceedsRateLimit(apiClient: ApiClient): Future[Boolean]
}

class Clock {
  def now = Time.now
}

class MemcachedBasedRateLimiter(
  memcachedClient: MemcachedClient,
  rateLimit: RateLimit,
  applicationName: ResourceName,
  clock: Clock
) extends RateLimiter {

  def exceedsRateLimit(apiClient: ApiClient): Future[Boolean] = {
    val clientSpecificRateLimit = ClientSpecificRateLimit(apiClient, rateLimit, applicationName)
    for {
      nrOfApiCalls <- memcachedClient.get(clientSpecificRateLimit.counterKey).map(_.map(_.toLong))
      reachedLimit = nrOfApiCalls.exists(_ == rateLimit.maximumNrOfRequests)
      _ <- Future.when(!reachedLimit) { incrementOrAdd(clientSpecificRateLimit) }
    } yield reachedLimit
  }

  private def incrementOrAdd(clientSpecificRateLimit: ClientSpecificRateLimit): Future[Long] = {
    val expiry = clock.now + clientSpecificRateLimit.rateLimit.ttl
    for {
      counter <- memcachedClient.incrementOrAdd(clientSpecificRateLimit.counterKey, expiry, 1L)
      _ <- Future.when(counter == 1) { memcachedClient.set(clientSpecificRateLimit.expiryKey, serializeTime(expiry))}
    } yield counter
  }

  private def serializeTime(time: Time): String = time.inNanoseconds.toString
  private def deserializeTime(s: String): Time = new Time(s.toLong)
}
