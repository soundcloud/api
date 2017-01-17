package com.soundcloud.publicApiStrangler.service

import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.client.mediaservice.WaveformUrl
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.soundcloud.publicApiStrangler.client.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.representation.{TrackRepresentation, TrackRepresentationAttachmentsUriDecorator, TrackRepresentationCommentCountDecorator, TrackRepresentationCountsDecorator, TrackRepresentationDomainLockingsDecorator, TrackRepresentationGeoblockingsDecorator, TrackRepresentationLabelDecorator, TrackRepresentationLike, TrackRepresentationQuotaDecorator, TrackRepresentationSecretTokenDecorator, TrackRepresentationSecretTokenUriParamDecorator, TrackRepresentationUserFavoriteDecorator, TrackRepresentationUserPlaybackCountDecorator, TrackRepresentationWaveformUrlDecorator}
import com.soundcloud.service.response.representation.{Geoblockings, User}

class TrackRepresentationBuilder {
  def build(userSession: UserSession,
            track: Track,
            user: User,
            isrc: Option[Isrc],
            counts: StitchCounts,
            label: Option[User],
            geoblockings: Option[Geoblockings],
            domainLockings: Seq[DomainLocking],
            trackAudioMetadata: TrackAudioMetadata,
            isLiked: Boolean,
            waveformUrls: Seq[WaveformUrl],
            secretTokenParameter: Option[String],
            downloadsPerTrack: Option[Int]): TrackRepresentationLike = {
    val basicTrackRep = TrackRepresentation(
      track = track,
      user = user,
      isrc = isrc,
      counts = counts,
      label = label,
      geoblockings = geoblockings,
      domainlockings = domainLockings,
      audioMetadata = trackAudioMetadata
    )

    val userIsOwner = track.user_urn == userSession.getUser

    var rep: TrackRepresentationLike = basicTrackRep
    // TODO Consider an "owning user" decorator
    if (userIsOwner)
      rep = TrackRepresentationSecretTokenDecorator(track, rep)
    if (userIsOwner || track.reveal_stats)
      rep = TrackRepresentationCountsDecorator(counts, rep)
    if ((userIsOwner || track.reveal_stats) && track.reveal_comments)
      rep = TrackRepresentationCommentCountDecorator(counts, rep)
    if (geoblockings.isDefined)
      rep = TrackRepresentationGeoblockingsDecorator(geoblockings.get, rep)
    if (domainLockings.nonEmpty)
      rep = TrackRepresentationDomainLockingsDecorator(domainLockings, rep)
    if (!userSession.isAnonymous) {
      rep = TrackRepresentationUserFavoriteDecorator(isLiked, rep)
      rep = TrackRepresentationUserPlaybackCountDecorator(rep)
    }
    secretTokenParameter.map { secret =>
      rep = TrackRepresentationSecretTokenUriParamDecorator(rep, secret)
    }
    label.map { label =>
      rep = TrackRepresentationLabelDecorator(label, rep)
    }
    rep = TrackRepresentationQuotaDecorator(track.downloadable, downloadsPerTrack, counts.download_count, userIsOwner, rep)
    rep = TrackRepresentationWaveformUrlDecorator(waveformUrls, rep)
    rep = TrackRepresentationAttachmentsUriDecorator(track.urn, rep) // TODO: make conditional on representation type
    rep
  }
}
