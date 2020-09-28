package com.soundcloud.publicApiStrangler.service

import com.soundcloud.publicApiStrangler.client.chrono.{ChronoItem, ChronoMeta, ChronoMetaParams, ChronoResponse}
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.TrackRequest
import com.soundcloud.publicApiStrangler.service.pagination.CursorBasedPagination
import com.soundcloud.publicApiStrangler.service.representation.collection.Collection
import com.soundcloud.publicApiStrangler.service.trackrepresentation._
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
      when(trackRepresentationsService.tracks(session, List(TrackRequest(track.visibleTrack.urn, None))))
        .thenReturn(Future.value(List(track)))
      when(trackmetadataClient.userTracks(session, trackOwnerUrn, pagination))
        .thenReturn(Future.value(chronoResponse))

      val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }

      "when data is not available" in new Context {
        when(trackRepresentationsService.tracks(session, List(TrackRequest(track.visibleTrack.urn, None))))
          .thenReturn(Future.value(List.empty))
        when(trackmetadataClient.userTracks(session, trackOwnerUrn, pagination))
          .thenReturn(Future.value(ChronoResponse.emptyResponse))

        val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, pagination))
        tracksCollection.items ==== List.empty
        tracksCollection.nextHref ==== None
      }
    }

    "#userTrack" >> {
      "when all data is available" in new Context {
        when(trackRepresentationsService.track(session, TrackRequest(track.visibleTrack.urn, Some("secr3t-Token"))))
          .thenReturn(Future.value(Some(track)))

        val trackRepresentation =
          Await.result(
            userTracksService
              .userTrack(track.visibleTrack.urn, session, track.user.urn.identifier, Some("secr3t-Token"))
          )

        trackRepresentation match {
          case Some(rep) => rep must beAnInstanceOf[TrackRepresentation]
          case _ => false
        }

      }

      "when data is not available" in new Context {
        when(trackRepresentationsService.track(session, TrackRequest(track.visibleTrack.urn, Some("secr3t-Token"))))
          .thenReturn(Future.value(None))

        val trackRepresentation =
          Await.result(userTracksService.userTrack(track.visibleTrack.urn, session, "123", Some("secr3t-Token")))

        trackRepresentation.isEmpty
      }
    }
  }
}
