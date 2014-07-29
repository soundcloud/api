package com.soundcloud.rollout

import java.util.concurrent.TimeUnit

import org.junit.runner.RunWith
import org.mockito.Mockito.times

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.UserSession
import com.soundcloud.scalakit.cache.Cache
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.util.Await
import com.twitter.util.Future

class RolloutRepositorySpec extends UnitSpecification {

  trait Context extends VerifiedMocks {
    val rolloutFeatureJson = withContentsOf("rollout", "feature")
    val rolloutMock = mock[JsonService]
    val cacheMock = mock[Cache]
    val repository = new RolloutRepository(rolloutMock, cacheMock)
    val session = mock[UserSession]
    val featureName = "PUBLIC_API_STRANGLER_CONTENT_AUTHORIZATION"
    val percentage = 2
  }

  trait CacheMissContext extends Context {
    override def after = {}
    override def before = {
      when(verified(cacheMock, times(2)).get(featureName)).thenReturn(Future(None))
      when(verified(rolloutMock).get(session, Path("/features") / s"$featureName.json", Map(), Map()))
        .thenReturn(Future(JsonResponse(OkStatus, rolloutFeatureJson)))
      when(verified(cacheMock).set(featureName, percentage.toString, 1, TimeUnit.MINUTES))
        .thenReturn(Future())
      super.before
    }
  }

  "fetches from the rollout system and caches the percentage" in new CacheMissContext {
    Await.result(repository.activated(session, featureName)) must beTrue
    Await.result(repository.activated(session, featureName)) must beFalse
  }

  trait CacheHitContext extends Context {
    override def before = {
      when(verified(cacheMock, times(3)).get(featureName)).thenReturn(Future(Some("2")))
      super.before
    }
  }

  "uses the cached percentage" in new CacheHitContext {
    Await.result(repository.activated(session, featureName)) must beTrue
    Await.result(repository.activated(session, featureName)) must beTrue
    Await.result(repository.activated(session, featureName)) must beFalse
  }
}
