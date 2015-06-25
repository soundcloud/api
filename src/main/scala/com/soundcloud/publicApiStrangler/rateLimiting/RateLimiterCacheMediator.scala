package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.core.AccessDependentRateLimit
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.{Future, Time, TimeFormat}
import org.jboss.netty.buffer.ChannelBuffers

class RateLimiterCacheMediator(cache: Cache, accessDependentRateLimit: AccessDependentRateLimit) {
  import RateLimiterCacheMediator._

  def alreadyReached: Future[Boolean] = {
    cache.get(accessDependentRateLimit.reachedCacheKey).map(_.isDefined)
  }

  def requestsMadeSoFar: Future[Option[Long]] = {
    cache.get(accessDependentRateLimit.counterCacheKey).map(_.map(_.toLong))
  }

  lazy val expiry: Future[Option[Time]] = {
    cache.get(accessDependentRateLimit.expiryCacheKey).map(_.map(deserializeTime))
  }

  def updateRequestCount: Future[Option[Long]] = {
    cache.incr(accessDependentRateLimit.counterCacheKey)
  }

  def establish: Future[Time] = {
    val expiry = Time.now + accessDependentRateLimit.suitableConfiguration.timeWindow.toTwitterDuration
    for {
      updatedRequestCount <- cache.add(accessDependentRateLimit.counterCacheKey, one, expiry)
      _ <- cache.set(accessDependentRateLimit.expiryCacheKey, serializeTime(expiry), expiry)
    } yield expiry
  }

  def markAsReached: Future[Unit] = {
    for {
      Some(e) <- expiry
      _ <- cache.set(accessDependentRateLimit.reachedCacheKey, "reached", e)
    } yield ()
  }
}

object RateLimiterCacheMediator {
  val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  def serializeTime(time: Time): String = timeFormat.format(time)
  def deserializeTime(s: String): Time = timeFormat.parse(s)
  val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
}
