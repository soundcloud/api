package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.service.client.SimilarSoundsClient
import com.soundcloud.service.response.representation.{SimilarSounds, SimilarSoundsMeta}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{times, verify, when}

class SimilarSoundsMapperSpec
  extends UnitSpecification {

  trait Context extends Scope {
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
    when(similarSoundsClientMock.fetchSimilar(anonymousSession, param, 1, 10, "", None)).
      thenReturn(Future.value(Some(returnedSimilarSounds)))

    // verify that entityMapper is called with fake results from mock client
    when(entityMapperMock.embed(List(new Urn("soundcloud:tracks:1")))).
      thenReturn(null)

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds)

    verify(similarSoundsClientMock).fetchSimilar(anonymousSession, param, 1, 10, "", None)
    verify(similarSoundsClientMock).fetchSimilar(anonymousSession, param, 1, 10, "", None)
  }

  "returns empty map if client responds with none" in new Context {
    // return 404 to simulate non existing track
    when(similarSoundsClientMock.fetchSimilar(anonymousSession, param, 1, 10, "", None)).
      thenReturn(Future.value(None))

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds) ==== None

    val emptyMap = similarSoundsMapper.mapNonEmptyInputs(anonymousSession, Set(page))
    Await.result(emptyMap) ==== Map.empty

    verify(similarSoundsClientMock, times(2)).fetchSimilar(anonymousSession, param, 1, 10, "", None)
  }

  "transforms offset based pagination to page based" in new Context {
    similarSoundsMapper.offsetBasedToPageBased(0, 10) ==== Tuple2(1, 10)
    similarSoundsMapper.offsetBasedToPageBased(0, 0) ==== Tuple2(0, 0)
    similarSoundsMapper.offsetBasedToPageBased(0, 10) ==== Tuple2(1, 10)
    similarSoundsMapper.offsetBasedToPageBased(10, 10) ==== Tuple2(2, 10)
    similarSoundsMapper.offsetBasedToPageBased(20, 10) ==== Tuple2(3, 10)
  }
}
