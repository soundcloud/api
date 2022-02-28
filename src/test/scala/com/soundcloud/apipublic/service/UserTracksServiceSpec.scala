package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.chrono.{ChronoItem, ChronoMeta, ChronoMetaParams, ChronoResponse}
import com.soundcloud.apipublic.client.trackmetadata.TrackmetadataClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation._
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when

class UserTracksServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val trackmetadataClient = mock[TrackmetadataClient]
    val pagination = CursorBasedPagination(
      "https://api.soundcloud.com",
      "/users/1/tracks/",
      ParamMap(),
      Some("2"),
      1
    )
    val userTracksService = new UserTracksService(
      trackRepresentationsService,
      trackmetadataClient
    )

    val track = createTrackRepresentation
    val access = AccessParams.defaultAccess

    val chronoResponse = ChronoResponse(
      items = List(ChronoItem("", "tracks", trackUrn, "2")),
      meta = ChronoMeta(
        params = ChronoMetaParams(
          cursor = None,
          limit = 0,
          direction = "desc"
        ),
        validForCaching = false
      )
    )
  }

  "#userTracks" >> {
    "when all data is available" in new Context {
      when(
        trackRepresentationsService
          .tracks(session, List(TrackRequest(track.urn, None)), access)
      ).thenReturn(Future.value(List(track)))
      when(trackmetadataClient.userTracks(session, trackOwnerUrn, pagination))
        .thenReturn(Future.value(chronoResponse))

      val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, access, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }

      "when data is not available" in new Context {
        when(
          trackRepresentationsService
            .tracks(session, List(TrackRequest(track.urn, None)), access)
        ).thenReturn(Future.value(List.empty))
        when(trackmetadataClient.userTracks(session, trackOwnerUrn, pagination))
          .thenReturn(Future.value(ChronoResponse.emptyResponse))

        val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, access, pagination))
        tracksCollection.items ==== List.empty
        tracksCollection.nextHref ==== None
      }
    }
  }
}
