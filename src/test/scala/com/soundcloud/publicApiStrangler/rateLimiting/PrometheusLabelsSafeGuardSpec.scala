package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.jvmkit.config.Config
import com.soundcloud.ratelimiting.core.ClientApplication
import com.soundcloud.scalakit.test.{InMemoryCache, UnitSpecification}
import com.soundcloud.scalakit.{ResourceName, Urn}
import com.twitter.util.Await

class PrometheusLabelsSafeGuardSpec extends UnitSpecification {

  "The safeguard" should {

    trait Context extends Scope {
      val appName = ResourceName("TEST")
      val config = mock[Config]
      config.get("RATELIMITING_MAX_PROMETHEUS_COUNTER_LABELS") returns "3"
      val cache = new InMemoryCache
      val safeGuard = new PrometheusLabelsSafeGuard(cache, config, appName)
      val client = ClientApplication(Urn("soundcloud", "applications", "123"))
    }

    "return the client identifier as a label if already contained in set" in new Context {
      Await.result(cache.set(safeGuard.Constants.uniqueClientsCacheKey, "234,123,900"))
      Await.result(safeGuard.getSafeLabel(client)) ==== "123"
    }

    "return the client identifier as a label and add to set if label limit not reached" in new Context {
      Await.result(safeGuard.getSafeLabel(client)) ==== "123"
      Await.result(cache.get(safeGuard.Constants.uniqueClientsCacheKey)) ==== Some("123")
    }

    "return 'other' as a label if not contained in set and limit has been reached" in new Context {
      Await.result(cache.set(safeGuard.Constants.uniqueClientsCacheKey, "234,249,900"))
      Await.result(safeGuard.getSafeLabel(client)) ==== "other"
      Await.result(cache.get(safeGuard.Constants.uniqueClientsCacheKey)) ==== Some("234,249,900")
    }
  }
}
