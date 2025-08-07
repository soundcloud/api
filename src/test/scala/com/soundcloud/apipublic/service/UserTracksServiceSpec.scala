package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.profile.ProfilesClient
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation._
import com.twitter.finagle.http.ParamMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import proto.soundcloud.profiles.api._

class UserTracksServiceSpec extends TrackRepresentationsSpecificationContext {

  trait Context extends TrackRepresentationsContext {

    val trackRepresentationsService = mock[TrackRepresentationsService]
    val proflesClient = mock[ProfilesClient]
    val limit = 2
    val cursor = ""
    val userTracksService = new UserTracksService(
      trackRepresentationsService,
      proflesClient
    )

    val track = createTrackRepresentation
    val access = AccessParams.defaultAccess

    val chronoResponse = ChronoResponse(
      items = List(ChronoItem(urn = trackUrn.toString)),
      meta = Some(
        ChronoMeta(
          params = Some(
            ChronoParams(
              direction = ChronoDirection.desc
            )
          )
        )
      )
    )
  }

  "#userTracks" >> {
    "when all data is available" in new Context {
      when(
        trackRepresentationsService
          .tracks(session, List(TrackRequest(track.urn, Option.empty)), access)
      ).thenReturn(Future.value(List(track)))
      when(proflesClient.fetchTracksUploadedByUserFromProfiles(session, trackOwnerUrn, limit, cursor))
        .thenReturn(Future.value(chronoResponse))

      val pagination = CursorBasedPagination(
        "https://api.soundcloud.com",
        "/me/tracks/",
        ParamMap(),
        Some(cursor),
        limit
      )
      val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, access, pagination))

      tracksCollection match {
        case rep =>
          rep must beAnInstanceOf[Collection[TrackRepresentation]]
      }

      "when data is not available" in new Context {
        when(
          trackRepresentationsService
            .tracks(session, List(TrackRequest(track.urn, Option.empty)), access)
        ).thenReturn(Future.value(List.empty))
        when(
          proflesClient
            .fetchTracksUploadedByUserFromProfiles(session, trackOwnerUrn, pagination.pageSize, pagination.cursor.get)
        ).thenReturn(Future.value(ChronoResponse.defaultInstance))

        val tracksCollection = Await.result(userTracksService.userTracks(session, trackOwnerUrn, access, pagination))
        tracksCollection.items ==== List.empty
        tracksCollection.nextHref ==== Option.empty
      }
    }
  }
}
