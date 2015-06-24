package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.publicApiStrangler.support.TimeConversions._
import com.soundcloud.ratelimiting.core.{ActionableAccessMechanism, MemcachedKeys, RateLimit}
import com.soundcloud.scalakit.cache.Cache
import com.twitter.util.{Future, Time, TimeFormat}
import org.jboss.netty.buffer.ChannelBuffers

class RateLimiterCacheMediator(cache: Cache, rateLimit: RateLimit, accessMechanism: ActionableAccessMechanism, applicationName: ResourceName) {
  import RateLimiterCacheMediator._

  private val memcachedKeys = MemcachedKeys.forTuple(accessMechanism, rateLimit, applicationName)

  def alreadyReached: Future[Boolean] = {
    cache.get(memcachedKeys.reached).map(_.isDefined)
  }

  def requestsMadeSoFar: Future[Option[Long]] = {
    cache.get(memcachedKeys.counter).map(_.map(_.toLong))
  }

  lazy val expiry: Future[Option[Time]] = {
    cache.get(memcachedKeys.expiry).map(_.map(deserializeTime))
  }

  def updateRequestCount: Future[Option[Long]] = {
    cache.incr(memcachedKeys.counter)
  }

  def establish: Future[Time] = {
    val expiry = Time.now + rateLimit.default.timeWindow.toTwitterDuration
    for {
      updatedRequestCount <- cache.add(memcachedKeys.counter, one, expiry)
      _ <- cache.set(memcachedKeys.expiry, serializeTime(expiry), expiry)
    } yield expiry
  }

  def markAsReached: Future[Unit] = {
    for {
      Some(e) <- expiry
      _ <- cache.set(memcachedKeys.reached, "reached", e)
    } yield ()
  }
}

object RateLimiterCacheMediator {
  val timeFormat = new TimeFormat("yyyy-MM-dd HH:mm:ss Z")
  def serializeTime(time: Time): String = timeFormat.format(time)
  def deserializeTime(s: String): Time = timeFormat.parse(s)
  val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
}
