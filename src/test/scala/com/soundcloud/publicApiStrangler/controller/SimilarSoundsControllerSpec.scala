package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.features.Rollout
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapping.similarsounds.SimilarSoundsMapping
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMappingMock
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.service.response.representation.SimilarSounds
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.bff.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.mockito.Mockito.times
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}

class SimilarSoundsControllerSpec
  extends InjectionBasedControllerSpecification {


  trait Context extends VerifiedMocks with Scope {
    val session = fakeUserAuthentication(anonymousSession)
    val similarSoundsMapperMock = mock[SimilarSoundsMapper]
    val rolloutMock = mock[Rollout]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val similarSoundsController = new SimilarSoundsController(session, similarSoundsMapperMock, "http://api.soundcloud.com", rolloutMock, mothershipDispatcherMock)

    val forwardStatus = HttpResponseStatus.FOUND.getCode
    val forwardContent = "forwardContent"

    def expectForwardedRequest =
      mothershipDispatcherMock.dispatch(any[Request])
        .returns(Future(new ResponseBuilder().body(forwardContent).status(forwardStatus)))

    def stillForwards(response: MockResponse) = {
      response.code ==== 302
      response.body ==== "forwardContent"
    }

    def fetchSimilarSounds(path: String, endpoint: String) = {
      val param = new Urn("soundcloud:tracks:123")
      val page = OffsetBasedPage(param, "http://api.soundcloud.com", endpoint, Map.empty[String, String], 3, 22)

      abstract class SimilarSoundsMock extends ObjectMappingMock[SimilarSounds] with SimilarSoundsMapping
      val similarSoundsMock = ObjectMappingMock.prepare[SimilarSoundsMock]

      when(verified(similarSoundsMapperMock).materialize(anonymousSession, page)).
        thenReturn(Future(Some(similarSoundsMock)))

      val response = get(similarSoundsController, path)
      response.code ==== 200
      there was noCallsTo(mothershipDispatcherMock)
    }
  }

  trait EnabledContext extends Context {
    when(verified(rolloutMock, times(1)).isActive(any[String]))
      .thenReturn(true)
  }

  trait DisabledContext extends Context {
    when(verified(rolloutMock, times(1)).isActive(any[String]))
      .thenReturn(false)
  }

  "forwards to Mothership when feature not enabled" in new DisabledContext {
    expectForwardedRequest
    val response = get(similarSoundsController, "/tracks/123/related", Map(), Map("Host" -> "api.soundcloud.com"))
    stillForwards(response)
  }

  "processes requests to /tracks/:trackId/related" in new EnabledContext {
    fetchSimilarSounds("/tracks/123/related?limit=22&offset=3", "/tracks/123/related")
  }

  "processes requests to /tracks/:trackId/related.json" in new EnabledContext {
    fetchSimilarSounds("/tracks/123/related.json?limit=22&offset=3", "/tracks/123/related.json")
  }

  "returns 404 if mapper returns none" in new EnabledContext {
    val param = new Urn("soundcloud:tracks:123")
    val page = OffsetBasedPage(param, "http://api.soundcloud.com", "/tracks/123/related", Map.empty[String, String], 3, 22)

    when(verified(similarSoundsMapperMock).materialize(anonymousSession, page)).
      thenReturn(Future.value(None))

    val response = get(similarSoundsController, "/tracks/123/related?limit=22&offset=3")
    response.code ==== 404
  }
}
