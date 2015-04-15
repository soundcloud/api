package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.cache.MemcachedClient
import com.twitter.util.{Time, Duration, Future}

case class ApiClient(value: Urn) {
  lazy val counterKey = ""
  lazy val expiryKey = ""
}

sealed trait RateLimit {
  def ttl: Duration
  def maximumNrOfRequests: Int
  def identifier: String
}
case class GeneralRateLimit(ttl: Duration, maximumNrOfRequests: Int) extends RateLimit {
  override def identifier: String = s""
}

trait RateLimiter {
  def exceedsRateLimit(apiClient: ApiClient): Future[Boolean]
}

class MemcachedBasedRateLimiter(memcachedClient: MemcachedClient, ttl: Duration, limit: Int) extends RateLimiter {


  def exceedsRateLimit(apiClient: ApiClient): Future[Boolean] = {
    for {
      nrOfApiCalls <- memcachedClient.get(apiClient.counterKey).map(_.map(_.toLong))
      reachedLimit = nrOfApiCalls.exists(_ == limit)
      _ <- when(!reachedLimit) { incrementOrAdd(apiClient, expiry(ttl)) }
    } yield reachedLimit
  }

  private def expiry(ttl: Duration): Time = Time.now + ttl

  private def incrementOrAdd(apiClient: ApiClient, expiry: Time): Future[Long] = {
    for {
      counter <- memcachedClient.incrementOrAdd(apiClient.counterKey, expiry, 1L)
      _ <- when(counter == 1) { memcachedClient.set(apiClient.expiryKey, serializeTime(expiry))}
    } yield counter
  }

  def when[A](cond: Boolean)(f: => Future[A]): Future[Unit] = {
    if (cond) f.map(_ => ()) else Future(())
  }

  private def serializeTime(time: Time): String = time.inNanoseconds.toString
  private def deserializeTime(s: String): Time = new Time(s.toLong)
}
