package com.soundcloud.publicApiStrangler.mapper.similarsounds

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.nextbff.pagination.OffsetBasedPage
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.SystemPlaylistsClient
import com.soundcloud.publicApiStrangler.mapper.search.SearchEntityMapper
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.similarSoundsNonEmpty
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{times, verify, when}

class SimilarSoundsMapperSpec
  extends UnitSpecification {

  trait Context extends Scope {
    val similarSoundsClientMock = mock[SystemPlaylistsClient]
    val entityMapperMock = mock[SearchEntityMapper]
    val similarSoundsMapper = new SimilarSoundsMapper(similarSoundsClientMock, entityMapperMock)
    val param = Urn("soundcloud", "tracks", "123")
    val page = OffsetBasedPage(param, "http://api.soundcloud.com", "tracks/123/relates.json", Map.empty[String, String], 0, 10)

    implicit val context = new MappingContext(mock[UserSession])
  }

  "maps similar sounds to json" in new Context {
    val returnedSimilarSounds = SimilarSounds(
      Seq(Urn("soundcloud", "tracks", "1")),
      SimilarSoundsMeta(50, "variant", "source", Urn("soundcloud", "systems", "123"))
    )

    // mock client returns fake result
    when(similarSoundsClientMock.fetchSimilar(anonymousSession, param)).
      thenReturn(Future.value(Some(returnedSimilarSounds)))

    // verify that entityMapper is called with fake results from mock client
    when(entityMapperMock.embed(List(Urn("soundcloud", "tracks", "1")))).
      thenReturn(null)

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds)

    verify(similarSoundsClientMock).fetchSimilar(anonymousSession, param)
    verify(similarSoundsClientMock).fetchSimilar(anonymousSession, param)
  }

  "returns empty map if client responds with none" in new Context {
    // return 404 to simulate non existing track
    when(similarSoundsClientMock.fetchSimilar(anonymousSession, param)).
      thenReturn(Future.value(None))

    val similarSounds = similarSoundsMapper.mapSingleInput(anonymousSession, page)
    Await.result(similarSounds) ==== None

    val emptyMap = similarSoundsMapper.mapNonEmptyInputs(anonymousSession, Set(page))
    Await.result(emptyMap) ==== Map.empty

    verify(similarSoundsClientMock, times(2)).fetchSimilar(anonymousSession, param)
  }

  "maps similar sounds response to objects" in {
    val expectedTracks = Seq(
      Urn("soundcloud", "tracks", "139565597"),
      Urn("soundcloud", "tracks", "113100217"),
      Urn("soundcloud", "tracks", "93685166")
    )

    val expectedMeta = SimilarSoundsMeta(
      pageSize = 50,
      variant = "default",
      sourceVersion = "snap-source",
      queryUrn = Urn("soundcloud", "similarsounds", "c90098b750d4470cafa834fb951fe657")
    )
    SimilarSoundsMapper(similarSoundsNonEmpty) ==== SimilarSounds(expectedTracks, expectedMeta)
  }
}
