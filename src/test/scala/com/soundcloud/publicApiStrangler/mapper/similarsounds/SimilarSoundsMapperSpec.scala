package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.scalakit.test.VerifiedMocks
import com.soundcloud.service.client.SimilarSoundsClient
import com.soundcloud.service.response.representation.{SimilarSounds, SimilarSoundsMeta}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._

class SimilarSoundsMapperSpec
  extends InjectionBasedControllerSpecification {

  trait Context extends Scope with VerifiedMocks {
    val similarSoundsClientMock = mock[SimilarSoundsClient]
    val entityMapperMock = mock[SearchEntityMapper]
    val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClientMock, entityMapperMock)
    val param = new Urn("soundcloud", "tracks", "123")
    val page = OffsetBasedPage(param, "http://api.soundcloud.com", "tracks/123/relates.json", Map.empty[String, String], 0, 10)

    implicit val context = new MappingContext(mock[UserSession])
  }

  "maps similar sounds to json" in new Context {
    val returnedSimilarSounds = SimilarSounds(
      Seq(new Urn("soundcloud:tracks:1")),
      SimilarSoundsMeta(0, 10, "variant", "source", new Urn("soundcloud:systems:123"), "", "")
    )

    // mock client returns fake result
    when(verified(similarSoundsClientMock).fetchSimilar(anonymousSession, param, 1, 10, "", None)).
      thenReturn(Future.value(Some(returnedSimilarSounds)))

    // verify that entityMapper is called with fake results from mock client
    when(verified(entityMapperMock).embed(List(new Urn("soundcloud:tracks:1")))).
      thenReturn(null)

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds)
  }

  "returns empty map if client responds with none" in new Context {
    // return 404 to simulate non existing track
    when(verified(similarSoundsClientMock, times(2)).fetchSimilar(anonymousSession, param, 1, 10, "", None)).
      thenReturn(Future.value(None))

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds) ==== None
    
    val emptyMap = similarSoundsMapper.mapNonEmptyInputs(anonymousSession, Set(page))
    Await.result(emptyMap) ==== Map.empty
  }

  "transforms offset based pagination to page based" in new Context {
    similarSoundsMapper.offsetBasedToPageBased(0, 10) ==== Tuple2(1, 10)
    similarSoundsMapper.offsetBasedToPageBased(0, 0) ==== Tuple2(0, 0)
    similarSoundsMapper.offsetBasedToPageBased(0, 10) ==== Tuple2(1, 10)
    similarSoundsMapper.offsetBasedToPageBased(10, 10) ==== Tuple2(2, 10)
    similarSoundsMapper.offsetBasedToPageBased(20, 10) ==== Tuple2(3, 10)
  }
}
