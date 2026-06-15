package com.soundcloud.apipublic.service

import com.soundcloud.apipublic.client.recentlyplayed.{RecentlyPlayedClient, RecentlyPlayedTrack}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentation, TrackRepresentationsService}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future

class RecentlyPlayedService(
    recentlyPlayedClient: RecentlyPlayedClient,
    trackRepresentationsService: TrackRepresentationsService
) {
  private val maxTracks = 25

  def recentlyPlayedTracks(
      session: UserSession,
      user: Urn,
      access: AccessParams
  ): Future[List[TrackRepresentation]] = {
    recentlyPlayedClient.getTracks(session, user, maxTracks).flatMap { recentlyPlayed =>
      val distinctRecentlyPlayed = distinctByUrn(recentlyPlayed)
      if (distinctRecentlyPlayed.isEmpty) {
        Future.value(Nil)
      } else {
        val trackRequests = distinctRecentlyPlayed.map(entry => TrackRequest(entry.urn, None))
        trackRepresentationsService.tracks(session, trackRequests, access).map { tracks =>
          val tracksByUrn = tracks.map(track => track.urn -> track).toMap
          distinctRecentlyPlayed.flatMap(entry => tracksByUrn.get(entry.urn))
        }
      }
    }
  }

  private def distinctByUrn(entries: List[RecentlyPlayedTrack]): List[RecentlyPlayedTrack] = {
    entries
      .foldLeft((List.empty[RecentlyPlayedTrack], Set.empty[Urn])) {
        case ((acc, seen), entry) =>
          if (seen.contains(entry.urn)) (acc, seen)
          else (entry :: acc, seen + entry.urn)
      }
      ._1
      .reverse
  }
}
