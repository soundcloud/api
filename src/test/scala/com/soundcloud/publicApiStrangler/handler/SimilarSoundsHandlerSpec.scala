package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.mapper.similarsounds.{SimilarSounds, SimilarSoundsMapper, SimilarSoundsMapping}
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMappingMock
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import org.mockito.Mockito.{verify, when}

class SimilarSoundsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val session = new FakeUserAuthentication(anonymousSession)
    val similarSoundsMapperMock = mock[SimilarSoundsMapper]
    val similarSoundsHandler = new SimilarSoundsHandler(session, similarSoundsMapperMock, "http://api.soundcloud.com")

    override def routingDefinitions = Routing.forSimilarSoundsHandler(similarSoundsHandler)

    val forwardStatus = HttpResponseStatus.FOUND.getCode
    val forwardContent = "forwardContent"

    def fetchSimilarSounds(path: String, endpoint: String) = {
      val param = Urn("soundcloud", "tracks", "123")
      val page = OffsetBasedPage(param, "http://api.soundcloud.com", endpoint, Map.empty[String, String], 3, 22)

      abstract class SimilarSoundsMock extends ObjectMappingMock[SimilarSounds] with SimilarSoundsMapping
      val similarSoundsMock = ObjectMappingMock.prepare[SimilarSoundsMock]

      when(similarSoundsMapperMock.materialize(anonymousSession, page)).thenReturn(Future(Some(similarSoundsMock)))

      val response = get(similarSoundsHandler.handleSimilarSoundsRequest, path)
      response.statusCode ==== 200

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
    val param = Urn("soundcloud", "tracks", "123")
    val page =
      OffsetBasedPage(param, "http://api.soundcloud.com", "/tracks/123/related", Map.empty[String, String], 3, 22)

    when(similarSoundsMapperMock.materialize(anonymousSession, page)).thenReturn(Future.value(None))

    val response = get(similarSoundsHandler.handleSimilarSoundsRequest, "/tracks/123/related?limit=22&offset=3")
    response.statusCode ==== 404

    verify(similarSoundsMapperMock).materialize(anonymousSession, page)
  }
}
