package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.ratelimiting.types.RateLimit
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.{TimeFormat, Time, Future}
import org.jboss.netty.buffer.ChannelBuffers
import com.soundcloud.publicApiStrangler.support.TimeConversions._

class RateLimiterCacheMediator(cache: Cache, rateLimit: RateLimit, apiClient: ApiClient, applicationName: ResourceName) {
  import RateLimiterCacheMediator._

  private val clientSpecificRateLimit = ClientSpecificRateLimit(apiClient, rateLimit, applicationName)

  def alreadyReached: Future[Boolean] = {
    cache.get(clientSpecificRateLimit.reachedCacheKey).map(_.isDefined)
  }

  def requestsMadeSoFar: Future[Option[Long]] = {
    cache.get(clientSpecificRateLimit.counterCacheKey).map(_.map(_.toLong))
  }

  lazy val expiry: Future[Option[Time]] = {
    cache.get(clientSpecificRateLimit.expiryCacheKey).map(_.map(deserializeTime))
  }

  def updateRequestCount: Future[Option[Long]] = {
    cache.incr(clientSpecificRateLimit.counterCacheKey)
  }

  def establish: Future[Time] = {
    val expiry = Time.now + clientSpecificRateLimit.rateLimit.timeWindow.toTwitterDuration
    for {
      updatedRequestCount <- cache.add(clientSpecificRateLimit.counterCacheKey, one, expiry)
      _ <- cache.set(clientSpecificRateLimit.expiryCacheKey, serializeTime(expiry))
    } yield expiry
  }

  def markAsReached: Future[Unit] = {
    for {
      Some(e) <- expiry
      _ <- cache.set(clientSpecificRateLimit.reachedCacheKey, "reached", e)
    } yield ()
  }
}

object RateLimiterCacheMediator {
  val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  def serializeTime(time: Time): String = timeFormat.format(time)
  def deserializeTime(s: String): Time = timeFormat.parse(s)
  val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
}
