package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.scalakit.cache.{Cache, MemcachedClient}
import com.twitter.util.{Future, Time, TimeFormat}
import org.jboss.netty.buffer.ChannelBuffers

class CacheBasedRateLimiter(cache: Cache, val rateLimit: RateLimit, applicationName: ResourceName) extends RateLimiter {

  def advanceRateLimitStatus(apiClient: ApiClient): Future[RateLimitStatus] = {
    val clientSpecificRateLimit = ClientSpecificRateLimit(apiClient, rateLimit, applicationName)
    for {
      requestsMadeSoFar <- cache.get(clientSpecificRateLimit.counterKey).map(_.map(_.toLong))
      status <- requestsMadeSoFar match {
        case Some(number) if number == rateLimit.maximumNrOfRequests =>
          for {
            serializedExpiry <- cache.get(clientSpecificRateLimit.expiryKey)
            expiry = serializedExpiry.map(deserializeTime)
          } yield RateLimitStatus.Reached(rateLimit.maximumNrOfRequests, expiry)
        case Some(_) =>
          def tolerateCornerCase(updatedCount: Option[Long]) = updatedCount.getOrElse(1L)
          for {
            updatedRequestCount <- cache.incr(clientSpecificRateLimit.counterKey).map(tolerateCornerCase)
            expiry <- cache.get(clientSpecificRateLimit.expiryKey).map(_.map(deserializeTime))
          } yield RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - updatedRequestCount, expiry)
        case None =>
          val expiry = Time.now + clientSpecificRateLimit.rateLimit.ttl
          val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
          for {
            updatedRequestCount <- cache.add(clientSpecificRateLimit.counterKey, one, expiry)
            _ <- cache.set(clientSpecificRateLimit.expiryKey, serializeTime(expiry))
          } yield RateLimitStatus.Advancing(rateLimit.maximumNrOfRequests - 1, Some(expiry))
      }
    } yield status
  }

  private val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  private def serializeTime(time: Time): String = timeFormat.format(time)
  private def deserializeTime(s: String): Time = timeFormat.parse(s)
}
