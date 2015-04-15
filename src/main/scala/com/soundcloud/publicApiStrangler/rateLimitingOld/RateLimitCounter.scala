package com.soundcloud.publicApiStrangler.rateLimitingOld

import com.twitter.util.{Time, Future}
import com.twitter.finagle.memcached.{Client => MemcachedClient}
import com.soundcloud.jvmkit.logging.SoundCloudLoggerFactory
import org.jboss.netty.buffer.ChannelBuffers
import scala.util.control.NonFatal

class RateLimitCounter(memcached: MemcachedClient) {
  val logger = SoundCloudLoggerFactory.getLogger(this.getClass)

  val one = ChannelBuffers.copiedBuffer("1".getBytes("UTF-8"))
  val whatToReturnWhenAnErrorHappens = Future.value(0L)
  val unusedFlag = 0

  def incr(entry: UsageEntry): Future[Long] = {
    memcached.incr(entry.key).flatMap {
      case Some(value) => Future.value(value: Long)
      case None => addToMemcached(entry)
    }.rescue {
      case NonFatal(e) => logger.error(s"Error updating memcached key [${entry.key}}]", e); whatToReturnWhenAnErrorHappens
    }
  }

  private def addToMemcached(usage: UsageEntry): Future[Long] = {
    val whenToExpire = Time.now + usage.time.unitOfMeasure
    memcached.add(usage.key, unusedFlag, whenToExpire, one).map(_ => 1)
  }
}
