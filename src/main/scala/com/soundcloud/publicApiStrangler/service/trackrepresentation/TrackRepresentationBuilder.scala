package com.soundcloud.publicApiStrangler.service.trackrepresentation

import java.net.URLEncoder

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track

import scala.collection.immutable.HashSet

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
  ): TrackRepresentation = {
    val userIsOwner = sessionUser.map(track.user_urn == _).getOrElse(false)
    val isAnonymous = sessionUser.isEmpty

    TrackRepresentation(
      track = track,
      user = user,
      isrc = isrc,
      label = label,
      geoblockings = getAvailableCountryNodes(geoblockings),
      domainlockings = getDomainLockings(domainLockings),
      audioMetadata = trackAudioMetadata,
      playbackCount = getCount(userIsOwner, track, "playback_count", counts),
      downloadCount = getCount(userIsOwner, track, "download_count", counts),
      favoritingsCount = getCount(userIsOwner, track, "favoritings_count", counts),
      repostsCount = getCount(userIsOwner, track, "reposts_count", counts),
      secretToken = getSecretTokenForPrivateTrack(track),
      releaseDay = releaseDayFor(track),
      releaseMonth = releaseMonthFor(track),
      uri = urlFor(track, getSecretTokenForPrivateTrack(track)),
      streamUrl = urlFor(track, "stream", getSecretTokenForPrivateTrack(track)),
      downloadUrl = urlFor(track, "download", getSecretTokenForPrivateTrack(track)),
      permalinkUrl = secretPath(track.permalink_url, track, getSecretTokenForPrivateTrack(track)),
      secretUri = getSecretUri(track),
      commentCount = getCommentCount(track, userIsOwner, counts),
      userFavourite = if (!isAnonymous) Some(isLiked) else None,
      userPlaybackCount = if (!isAnonymous) Some(1) else None,
      waveformUrl = waveformUrl.pngUrl.s,
      downloadable = getDownloadable(track, downloadsPerTrack, counts),
      downloadsRemaining = getDownloadsRemaining(counts, downloadsPerTrack, userIsOwner)
    )
  }
  private val baseUrl = "https://api.soundcloud.com/tracks"

  private def releaseDayFor(track: Track): Option[Int] =
    track.release_year.map(_ => track.release_day.getOrElse(1))

  private def releaseMonthFor(track: Track): Option[Int] =
    track.release_year.map(_ => track.release_month.getOrElse(1))

  private def getSecretTokenForPrivateTrack(track: Track): Option[String] =
    if (!track.public) Some(track.secret_token) else None

  private def getSecretUri(track: Track): Option[String] = {
    if (!track.public)
      Some(s"https://api.soundcloud.com/tracks/${track.urn.identifier}?secret_token=${track.secret_token}")
    else None
  }

  private def getCount(userIsOwner: Boolean, track: Track, countType: String, counts: StitchCounts): Option[Int] = {
    if (userIsOwner || track.reveal_stats)
      countType match {
        case "playback_count" => Some(counts.playback_count)
        case "download_count" => Some(counts.download_count)
        case "favoritings_count" => Some(counts.favoritings_count)
        case "reposts_count" => Some(counts.reposts_count)
        case _ => None
      }
    else None
  }

  private def getCommentCount(track: Track, userIsOwner: Boolean, counts: StitchCounts): Option[Int] = {
    if ((userIsOwner || track.reveal_stats) && track.reveal_comments)
      Some(counts.comment_count)
    else None
  }

  private def getAvailableCountryNodes(geoblockings: Geoblockings): Option[HashSet[String]] = {
    if (!geoblockings.isEmpty)
      Some(Country.officiallyAssignedAlpha2Codes.--(geoblockings))
    else None
  }

  private def getDomainLockings(domainLockings: Seq[DomainLocking]): Option[Seq[DomainLocking]] = {
    if (!domainLockings.isEmpty) {
      Some(domainLockings)
    } else {
      None
    }

  }

  private def urlFor(track: Track, subresource: String, secretParam: Option[String]) =
    secretUrl(s"${baseUrl}/${track.urn.identifier.toLong}/$subresource", track, secretParam)

  private def urlFor(track: Track, secretParam: Option[String]) =
    secretUrl(s"${baseUrl}/${track.urn.identifier.toLong}", track, secretParam)

  private def secretUrl(url: String, track: Track, secretParam: Option[String]): Option[String] = {
    if (!track.public && !secretParam.isEmpty) {
      val secret = URLEncoder.encode(secretParam.get, "UTF-8")
      Some(s"${url}?secret_token=$secret")
    } else {
      Some(url)
    }
  }

  private def secretPath(path: Option[String], track: Track, secretParam: Option[String]): Option[String] = {
    path
      .map(p => {
        if (!track.public && !secretParam.isEmpty) {
          val secret = URLEncoder.encode(secretParam.get, "UTF-8")
          Some(s"${p}/$secret")
        } else {
          Some(p)
        }
      })
      .getOrElse(None)

  }

  private def getDownloadable(track: Track, downloadsPerTrack: Option[Int], counts: StitchCounts) = {
    val trackDownloadable = track.downloadable.getOrElse(false)

    (trackDownloadable, downloadsPerTrack) match {
      case (false, _) => false
      case (true, None) => trackDownloadable // User has no quota, default to track's 'downloadable' setting
      case (true, Some(quota)) => counts.download_count < quota
    }
  }

  private def getDownloadsRemaining(
      counts: StitchCounts,
      downloadsPerTrack: Option[Int],
      userIsOwner: Boolean
  ): Option[Int] = {
    downloadsPerTrack.map(_ - counts.download_count) match {
      case res @ Some(_) if userIsOwner => res
      case _ => None
    }
  }

}
