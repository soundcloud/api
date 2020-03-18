package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track

class TrackRepresentationBuilder {
  def build(
      sessionUser: Option[Urn],
      track: Track,
      user: User,
      isrc: Option[Isrc],
      counts: StitchCounts,
      label: Option[User],
      geoblockings: Geoblockings,
      domainLockings: Seq[DomainLocking],
      trackAudioMetadata: TrackAudioMetadata,
      isLiked: Boolean,
      waveformUrl: TrackWaveformUrl,
      downloadsPerTrack: Option[Int]
  ): TrackRepresentationLike = {
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

    val userIsOwner = sessionUser.map(track.user_urn == _).getOrElse(false)
    val isAnonymous = sessionUser.isEmpty

    var rep: TrackRepresentationLike = basicTrackRep
    if (userIsOwner)
      rep = TrackRepresentationOwnerOnlyDecorator(track, rep)
    if (userIsOwner || track.reveal_stats)
      rep = TrackRepresentationCountsDecorator(counts, rep)
    if ((userIsOwner || track.reveal_stats) && track.reveal_comments)
      rep = TrackRepresentationCommentCountDecorator(counts, rep)
    if (!geoblockings.isEmpty)
      rep = TrackRepresentationGeoblockingsDecorator(geoblockings, rep)
    if (domainLockings.nonEmpty)
      rep = TrackRepresentationDomainLockingsDecorator(domainLockings, rep)
    if (!isAnonymous) {
      rep = TrackRepresentationUserFavoriteDecorator(isLiked, rep)
      rep = TrackRepresentationUserPlaybackCountDecorator(rep)
    }
    // The track access has been authorised at this point. If a user can see a private track, it is because they have
    // an appropriate token, either for the track or containing playlist. This decorator overrides the urls
    // with an additional secret token of the track.
    if (!track.public) {
      rep = TrackRepresentationPrivateUrlsDecorator(rep, track.secret_token)
    }
    label.map { label =>
      rep = TrackRepresentationLabelDecorator(label, rep)
    }
    rep =
      TrackRepresentationQuotaDecorator(track.downloadable, downloadsPerTrack, counts.download_count, userIsOwner, rep)
    rep = TrackRepresentationWaveformUrlDecorator(waveformUrl, rep)
    rep
  }
}
