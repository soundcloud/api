package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, TracksClient, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
import com.twitter.util.Future
import proto.soundcloud.tracks.api.{GetVisibleTracksRequest, TracksService, TrackRequest => TwirpTrackRequest}

import scala.util.control.NonFatal

class TrackVisibilityService(
    tracksClient: TracksClient,
    tracksTwinagleClient: TracksService,
    visibleTrackMapper: VisibleTrackMapper,
    telemetry: Telemetry,
    exceptionCollector: ExceptionCollector,
    whitelistedClients: Set[Urn]
) {
  private val inconsistentUserTracksFetchResponsesCounter =
    telemetry.counter(
      "inconsistent_tracks_fetch_response_total",
      "Count of inconsistent responses from json and twinagle tracks fetch after mapping",
      "one_side_empty"
    )

  def tracks(session: UserSession, trackRequests: List[TrackRequest]): Future[List[VisibleTrack]] = {
    for {
      visibleTracks <- Future
        .join(
          tracksClient.visibleTracks(session, trackRequests),
          visibleTracksTwinagle(session, trackRequests).handleAndReport(exceptionCollector) {
            case NonFatal(_) => List.empty
          }
        )
        .map {
          case (jsonTracks, twinagleTracks) =>
            compareAndReportTracks(jsonTracks, twinagleTracks)
            jsonTracks
        }

      filteredVisibleTracks = visibleTracks.filter { track =>
        track.disabledAt.isEmpty && // Filters tracks that are disabled (taken down or over quota)
        track.transcodings.exists(_.mimeType == "audio/mpeg") && // Filters out non playable tracks (missing transcoding)
        track.authorization.policy != ContentPolicy.BLOCK &&
        (whitelistedClients.contains(session.getAgent) || !isPaywalledTrack(track)) // Filters out paywalled tracks unless client is whitelisted
      }
    } yield {
      filteredVisibleTracks
    }
  }

  private def isPaywalledTrack(track: VisibleTrack): Boolean = {
    track.authorization.policy == ContentPolicy.MONETIZE &&
    track.authorization.getMonetizationModel == MonetizationModel.SUB_HIGH_TIER
  }

  private def visibleTracksTwinagle(
      session: UserSession,
      trackRequests: Seq[TrackRequest]
  ): Future[List[VisibleTrack]] = {
    val request = GetVisibleTracksRequest(
      trackRequests = trackRequests.toList.map(trackRequest =>
        TwirpTrackRequest(trackRequest.urn.toString, trackRequest.secretToken)
      ),
      userSession = Some(session.asProtoSession)
    )

    tracksTwinagleClient.getVisibleTracks(request).map { tracksResponse =>
      tracksResponse.tracks.toList
        .map(visibleTrackMapper.apply)
    }
  }

  private def compareAndReportTracks(
      tracks1: List[VisibleTrack],
      tracks2: List[VisibleTrack]
  ): Unit = {
    if (tracks1 != tracks2) {
      val oneSideEmpty = if (tracks1.isEmpty || tracks2.isEmpty) "true" else "false"

      inconsistentUserTracksFetchResponsesCounter.labels(oneSideEmpty).inc()
      val logger = SoundCloudLoggerFactory.getLogger(getClass)
      if (tracks1.length != tracks2.length)
        logger.warn(s"Tracks length inconsistency: ${tracks1.length} != ${tracks2.length}")
      else
        logger.warn(s"Track inconsistency: ${tracks1} != ${tracks2}")
    }
  }
}
