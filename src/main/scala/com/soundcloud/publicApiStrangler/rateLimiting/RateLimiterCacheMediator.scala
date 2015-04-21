package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.{TimeFormat, Time, Future}
import org.jboss.netty.buffer.ChannelBuffers

class RateLimiterCacheMediator(cache: Cache, rateLimit: RateLimit, apiClient: ApiClient, applicationName: ResourceName) {
  import RateLimiterCacheMediator._

  private val clientSpecificRateLimit = ClientSpecificRateLimit(apiClient, rateLimit, applicationName)

  def alreadyReached: Future[Boolean] = {
    cache.get(clientSpecificRateLimit.reachedKey).map(_.isDefined)
  }

  def requestsMadeSoFar: Future[Option[Long]] = {
    cache.get(clientSpecificRateLimit.counterKey).map(_.map(_.toLong))
  }

  lazy val expiry: Future[Option[Time]] = {
    cache.get(clientSpecificRateLimit.expiryKey).map(_.map(deserializeTime))
  }

  def updateRequestCount: Future[Long] = {
    cache.incr(clientSpecificRateLimit.counterKey).map(_.get)
  }

  def establish: Future[Time] = {
    val expiry = Time.now + clientSpecificRateLimit.rateLimit.ttl
    for {
      updatedRequestCount <- cache.add(clientSpecificRateLimit.counterKey, one, expiry)
      _ <- cache.set(clientSpecificRateLimit.expiryKey, serializeTime(expiry))
    } yield expiry
  }

  def markAsReached: Future[Unit] = {
    for {
      e <- expiry
      _ <- cache.set(clientSpecificRateLimit.reachedKey, "reached", e.get)
    } yield ()
  }
}

object RateLimiterCacheMediator {
  val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  def serializeTime(time: Time): String = timeFormat.format(time)
  def deserializeTime(s: String): Time = timeFormat.parse(s)
  val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
}
