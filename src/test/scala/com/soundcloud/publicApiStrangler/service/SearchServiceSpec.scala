package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.search.{Doc, SearchClient, SearchResponse}
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackPagination,
  TrackRepresentationsService,
  TrackRepresentationsSpecificationContext,
  TracksCollection
}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import com.soundcloud.outcome._

class SearchServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val searchClient = mock[SearchClient]
    val trackPagination = mock[TrackPagination]

    val trackRepresentationMock = createTrackRepresentation
    val searchService = new SearchService(
      trackRepresentationsService,
      searchClient
    )
  }

  "#searchTracks" >> {
    "when all data is available" in new Context {
      val query = "foo"
      val queryUrn = Urn("soundcloud", "search", "foo")

      when(trackRepresentationsService.tracks(session, List(TrackRequest(trackRepresentationMock.track.urn, None))))
        .thenReturn(Future.value(List(trackRepresentationMock)))
      when(
        searchClient.searchTracks(
          ===(session),
          ===(Params("q" -> query, "filter.content_tier" -> "FREE", "filter.content_country" -> "--")),
          anyObject
        )
      ).thenReturn(
        SearchResponse(
          query,
          queryUrn,
          0,
          5,
          1,
          1000,
          Seq(Doc(trackUrn)),
          None
        ).goodF
      )

      val result = Await.result(searchService.searchTracks(session, Map("q" -> query), trackPagination).value)

      val tracksCollection = result.getOrElse(TracksCollection(List.empty, None))
      tracksCollection.tracks ==== List(trackRepresentationMock)
    }
  }
}
