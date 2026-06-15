package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.recentlyplayed.{RecentlyPlayedClient, RecentlyPlayedTrack}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.trackrepresentation.{
  TrackRepresentationSpecContext,
  TrackRepresentationsService
}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.{Await, Future}

class RecentlyPlayedServiceSpec extends UnitSpecification with TrackRepresentationSpecContext {

  trait Context extends org.specs2.specification.Scope {
    val recentlyPlayedClient = mock[RecentlyPlayedClient]
    val trackRepresentationsService = mock[TrackRepresentationsService]
    val session = mock[UserSession]
    val user = Urn("soundcloud", "users", "1")
    val access = AccessParams.explicitAccess
    val recentlyPlayed = List(
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "2"), 2L),
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "1"), 1L)
    )
    val trackRepresentation1 = createTrackRepresentationFromVisibleTrack().copy(urn = Urn("soundcloud", "tracks", "1"))
    val trackRepresentation2 = createTrackRepresentationFromVisibleTrack().copy(urn = Urn("soundcloud", "tracks", "2"))
    val service = new RecentlyPlayedService(recentlyPlayedClient, trackRepresentationsService)
  }

  "fetches recently played tracks" in new Context {
    recentlyPlayedClient.getTracks(session, user, 25) returns Future.value(recentlyPlayed)
    trackRepresentationsService.tracks(
      session,
      List(TrackRequest(Urn("soundcloud", "tracks", "2"), None), TrackRequest(Urn("soundcloud", "tracks", "1"), None)),
      access
    ) returns Future.value(List(trackRepresentation1, trackRepresentation2))

    Await.result(service.recentlyPlayedTracks(session, user, access)) ====
      List(trackRepresentation2, trackRepresentation1)
  }

  "deduplicates tracks by urn" in new Context {
    val duplicateRecentlyPlayed = List(
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "2"), 3L),
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "1"), 2L),
      RecentlyPlayedTrack(Urn("soundcloud", "tracks", "2"), 1L)
    )

    recentlyPlayedClient.getTracks(session, user, 25) returns Future.value(duplicateRecentlyPlayed)
    trackRepresentationsService.tracks(
      session,
      List(TrackRequest(Urn("soundcloud", "tracks", "2"), None), TrackRequest(Urn("soundcloud", "tracks", "1"), None)),
      access
    ) returns Future.value(List(trackRepresentation1, trackRepresentation2))

    Await.result(service.recentlyPlayedTracks(session, user, access)) ====
      List(trackRepresentation2, trackRepresentation1)
  }

  "omits tracks that are not visible" in new Context {
    recentlyPlayedClient.getTracks(session, user, 25) returns Future.value(recentlyPlayed)
    trackRepresentationsService.tracks(
      session,
      List(TrackRequest(Urn("soundcloud", "tracks", "2"), None), TrackRequest(Urn("soundcloud", "tracks", "1"), None)),
      access
    ) returns Future.value(List(trackRepresentation2))

    Await.result(service.recentlyPlayedTracks(session, user, access)) ==== List(trackRepresentation2)
  }
}
