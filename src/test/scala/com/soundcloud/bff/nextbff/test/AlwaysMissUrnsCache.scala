package com.soundcloud.bff.nextbff.test

import java.util.concurrent.TimeUnit

import com.soundcloud.bff.nextbff.repository.UrnsCache
import com.soundcloud.jvmkit.module.memcached.Cache
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.io.Buf
import com.twitter.util.{Future, Time}
import play.api.libs.json.JsObject

class AlwaysMissUrnsCache extends UrnsCache(AlwaysMissCache) {

  override def get(urns: List[Urn],
                   bulkFetch: Set[Urn] => Future[Map[Urn, JsObject]],
                   cacheExpirationTimeMinutes: Int,
                   cacheAllowed: JsObject => Boolean) =
    bulkFetch(urns.toSet)
}

object AlwaysMissCache extends Cache {
  override def set(key: String, value: String) = Future.True

  override def set(key: String, value: String, ttl: Long, ttlUnit: TimeUnit) = Future.True

  override def set(keysAndValues: Map[String, Array[Byte]], ttl: Long, ttlUnit: TimeUnit) = Future.True

  override def set(key: String, value: String, expiry: Time) = Future.True

  override def get(key: String) = Future.None

  override def add(key: String, value: Buf, ttl: Time): Future[Boolean] = Future.True

  override def add(key: String, value: Buf): Future[Boolean] = Future.True

  override def incr(key: String): Future[Option[Long]] = Future.None

  override def incr(key: String, delta: Long): Future[Option[Long]] = Future.None

  override def decr(key: String): Future[Option[Long]] = Future.None

  override def decr(key: String, delta: Long): Future[Option[Long]] = Future.None

  override def get(keys: Iterable[String]) = Future.value(keys.zip(None).toMap)

  override def getBytes(keys: Iterable[String]): Future[Map[String, Array[Byte]]] = Future.value(Map.empty)

  override def remove(key: String) = Future.False
}

