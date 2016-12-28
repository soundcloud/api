package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.mapper.similarsounds.SimilarSoundsMapper
import com.soundcloud.publicApiStrangler.mapping.similarsounds.SimilarSoundsMapping
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMappingMock
import com.soundcloud.service.response.representation.SimilarSounds
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.mockito.Mockito.{when, verify}

class SimilarSoundsControllerSpec
  extends InjectionBasedControllerSpecification {


  trait Context extends Scope {
    val session = fakeUserAuthentication(anonymousSession)
    val similarSoundsMapperMock = mock[SimilarSoundsMapper]
    val similarSoundsController = new SimilarSoundsController(session, similarSoundsMapperMock, "http://api.soundcloud.com")

    val forwardStatus = HttpResponseStatus.FOUND.getCode
    val forwardContent = "forwardContent"

    def fetchSimilarSounds(path: String, endpoint: String) = {
      val param = new Urn("soundcloud:tracks:123")
      val page = OffsetBasedPage(param, "http://api.soundcloud.com", endpoint, Map.empty[String, String], 3, 22)

      abstract class SimilarSoundsMock extends ObjectMappingMock[SimilarSounds] with SimilarSoundsMapping
      val similarSoundsMock = ObjectMappingMock.prepare[SimilarSoundsMock]

      when(similarSoundsMapperMock.materialize(anonymousSession, page)).
        thenReturn(Future(Some(similarSoundsMock)))

      val response = get(similarSoundsController, path)
      response.code ==== 200

      verify(similarSoundsMapperMock).materialize(anonymousSession, page)
    }
  }

  "processes requests to /tracks/:trackId/related" in new Context {
    fetchSimilarSounds("/tracks/123/related?limit=22&offset=3", "/tracks/123/related")
  }

  "processes requests to /tracks/:trackId/related.json" in new Context {
    fetchSimilarSounds("/tracks/123/related.json?limit=22&offset=3", "/tracks/123/related.json")
  }

  "returns 404 if mapper returns none" in new Context {
    val param = new Urn("soundcloud:tracks:123")
    val page = OffsetBasedPage(param, "http://api.soundcloud.com", "/tracks/123/related", Map.empty[String, String], 3, 22)

    when(similarSoundsMapperMock.materialize(anonymousSession, page)).
      thenReturn(Future.value(None))

    val response = get(similarSoundsController, "/tracks/123/related?limit=22&offset=3")
    response.code ==== 404

    verify(similarSoundsMapperMock).materialize(anonymousSession, page)
  }
}
