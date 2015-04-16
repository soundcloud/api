package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.util.{Future, Time, TimeFormat}
import org.jboss.netty.buffer.ChannelBuffers

class MemcachedBasedRateLimiter(
  memcachedClient: MemcachedClient,
  rateLimit: RateLimit,
  applicationName: ResourceName,
  clock: Clock
) extends RateLimiter {

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val clientSpecificRateLimit = ClientSpecificRateLimit(apiClient, rateLimit, applicationName)
    for {
      requestsMadeSoFar <- memcachedClient.get(clientSpecificRateLimit.counterKey).map(_.map(_.toLong))
      status <- requestsMadeSoFar match {
        case Some(number) if number == rateLimit.maximumNrOfRequests =>
          for {
            serializedExpiry <- memcachedClient.get(clientSpecificRateLimit.expiryKey)
            expiry = serializedExpiry.map(deserializeTime)
          } yield RateLimitStatus.Reached(expiry)
        case Some(_) =>
          def tolerateCornerCase(updatedCount: Option[Long]) = updatedCount.getOrElse(1L)
          for {
            updatedRequestCount <- memcachedClient.incr(clientSpecificRateLimit.counterKey).map(tolerateCornerCase)
            expiry <- memcachedClient.get(clientSpecificRateLimit.expiryKey).map(_.map(deserializeTime))
          } yield RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - updatedRequestCount, expiry)
        case None =>
          val expiry = clock.now + clientSpecificRateLimit.rateLimit.ttl
          val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
          for {
            updatedRequestCount <- memcachedClient.add(clientSpecificRateLimit.counterKey, one, expiry)
            _ <- memcachedClient.set(clientSpecificRateLimit.expiryKey, serializeTime(expiry))
          } yield RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - 1, Some(expiry))
      }
    } yield status
  }

  private val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  private def serializeTime(time: Time): String = timeFormat.format(time)
  private def deserializeTime(s: String): Time = timeFormat.parse(s)
}
