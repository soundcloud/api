package com.soundcloud.publicApiStrangler.handler

import java.net.URL

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.SimilarTracksService
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackPagination, TrackRepresentationSpecContext}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.util.Future
import org.mockito.Mockito.when

class SimilarTracksHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {
  trait Context extends HandlerSpecificationScope {
    val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val trackUrn = Urn("soundcloud", "tracks", "1")
    val userAuthentication = new FakeUserAuthentication(session)

    val similarTracksService = mock[SimilarTracksService]
    val baseUrl = "https://api.soundcloud.com"

    val similarTracksHandler =
      new SimilarTracksHandler(userAuthentication, similarTracksService, baseUrl)

    def paginationParams(path: String) =
      TrackPagination(
        Some(1),
        Some(2),
        false,
        None,
        None,
        new URL(baseUrl + path)
      )

    override def routingDefinitions = Routing.forSimilarTracksHandler(similarTracksHandler)

    val mockTrackRepresentation = createTrackRepresentationFromVisibleTrack()
    val mocktracksCollection = Collection(List(mockTrackRepresentation), None)
  }

  "processes requests to /tracks/:trackId/related" in new Context {
    val paginationParams = "?limit=1&offset=2"
    val path = s"/tracks/1/related"

    when(
      similarTracksService
        .similarTracks(session, trackUrn, AccessParams.defaultAccess, paginationParams(path + paginationParams))
    ).thenReturn(Future(Some(mocktracksCollection)))

    val response = get(path + paginationParams)
    response.statusCode ==== 200
  }

  "returns 404 if mapper returns none" in new Context {
    val paginationParams = "?limit=1&offset=2"
    val path = "/tracks/123/related"

    when(
      similarTracksService
        .similarTracks(
          session,
          Urn("soundcloud", "tracks", "123"),
          AccessParams.defaultAccess,
          paginationParams(path + paginationParams)
        )
    ).thenReturn(Future(None))

    val response = get(path + paginationParams)
    response.statusCode ==== 404
  }
}
