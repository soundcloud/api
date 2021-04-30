package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.AllowlistedClients
import com.soundcloud.publicApiStrangler.authorization.policies.{Access, ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, UserRepresentation}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.tracks.VisibleTrack

import java.net.URLEncoder
import scala.collection.immutable.HashSet

class TrackRepresentationBuilder {
  def build(
      client: Option[Urn],
      sessionUser: Option[Urn],
      visibleTrack: VisibleTrack,
      user: UserRepresentation,
      isrc: Option[Isrc],
      counts: StitchCounts,
      label: Option[UserRepresentation],
      geoblockings: Geoblockings,
      trackAudioMetadata: TrackAudioMetadata,
      isLiked: Boolean,
      waveformUrl: TrackWaveformUrl,
      downloadsPerTrack: Option[Int]
  ): TrackRepresentation = {
    val userIsOwner = sessionUser.contains(visibleTrack.userUrn)
    val isAnonymous = sessionUser.isEmpty

    TrackRepresentation(
      visibleTrack = visibleTrack,
      user = user,
      isrc = isrc,
      label = label,
      geoblockings = getAvailableCountryNodes(geoblockings),
      audioMetadata = trackAudioMetadata,
      playbackCount = getCount(userIsOwner, visibleTrack, "playback_count", counts),
      downloadCount = getCount(userIsOwner, visibleTrack, "download_count", counts),
      favoritingsCount = getCount(userIsOwner, visibleTrack, "favoritings_count", counts),
      repostsCount = getCount(userIsOwner, visibleTrack, "reposts_count", counts),
      secretToken = getSecretTokenForPrivateTrack(visibleTrack),
      releaseDay = releaseDayFor(visibleTrack),
      releaseMonth = releaseMonthFor(visibleTrack),
      uri = urlFor(visibleTrack, getSecretTokenForPrivateTrack(visibleTrack)),
      streamUrl = getStreamUrl(visibleTrack),
      downloadUrl = urlFor(visibleTrack, "download", getSecretTokenForPrivateTrack(visibleTrack)),
      permalinkUrl = secretPath(visibleTrack.permalinkUrl, visibleTrack, getSecretTokenForPrivateTrack(visibleTrack)),
      secretUri = getSecretUri(visibleTrack),
      commentCount = getCommentCount(visibleTrack, userIsOwner, counts),
      userFavourite = if (!isAnonymous) Some(isLiked) else None,
      userPlaybackCount = if (!isAnonymous) Some(1) else None,
      waveformUrl = waveformUrl.pngUrl.s,
      downloadable = getDownloadable(visibleTrack, downloadsPerTrack, counts),
      downloadsRemaining = getDownloadsRemaining(counts, downloadsPerTrack, userIsOwner),
      policy = getPolicy(visibleTrack.authorization.policy, client),
      monetizationModel = getMonetizationModel(visibleTrack.authorization.monetizationModel, client)
    )
  }
  private val baseUrl = "https://api.soundcloud.com/tracks"

  private def releaseDayFor(visibleTrack: VisibleTrack): Option[Int] =
    visibleTrack.releaseYear.map(_ => visibleTrack.releaseDay.getOrElse(1))

  private def releaseMonthFor(visibleTrack: VisibleTrack): Option[Int] =
    visibleTrack.releaseYear.map(_ => visibleTrack.releaseMonth.getOrElse(1))

  private def getSecretTokenForPrivateTrack(visibleTrack: VisibleTrack): Option[String] =
    if (!visibleTrack.public) visibleTrack.secretToken else None

  private def getSecretUri(visibleTrack: VisibleTrack): Option[String] = {
    if (!visibleTrack.public)
      visibleTrack.secretToken.map(token =>
        s"https://api.soundcloud.com/tracks/${visibleTrack.urn.identifier}?secret_token=$token"
      )
    else None
  }

  private def getCount(
      userIsOwner: Boolean,
      visibleTrack: VisibleTrack,
      countType: String,
      counts: StitchCounts
  ): Option[Int] = {
    if (userIsOwner || visibleTrack.revealStats)
      countType match {
        case "playback_count" => Some(counts.playback_count)
        case "download_count" => Some(counts.download_count)
        case "favoritings_count" => Some(counts.favoritings_count)
        case "reposts_count" => Some(counts.reposts_count)
        case _ => None
      }
    else None
  }

  private def getCommentCount(visibleTrack: VisibleTrack, userIsOwner: Boolean, counts: StitchCounts): Option[Int] = {
    if ((userIsOwner || visibleTrack.revealStats) && visibleTrack.revealComments)
      Some(counts.comment_count)
    else None
  }

  private def getAvailableCountryNodes(geoblockings: Geoblockings): Option[HashSet[String]] = {
    if (geoblockings.nonEmpty)
      Some(Country.officiallyAssignedAlpha2Codes.--(geoblockings))
    else None
  }

  private def urlFor(visibleTrack: VisibleTrack, subresource: String, secretParam: Option[String]) =
    secretUrl(s"$baseUrl/${visibleTrack.urn.identifier.toLong}/$subresource", visibleTrack, secretParam)

  private def urlFor(visibleTrack: VisibleTrack, secretParam: Option[String]) =
    secretUrl(s"$baseUrl/${visibleTrack.urn.identifier.toLong}", visibleTrack, secretParam)

  private def secretUrl(url: String, visibleTrack: VisibleTrack, secretParam: Option[String]): Option[String] = {
    if (!visibleTrack.public && secretParam.isDefined) {
      val secret = URLEncoder.encode(secretParam.get, "UTF-8")
      Some(s"$url?secret_token=$secret")
    } else {
      Some(url)
    }
  }

  private def secretPath(
      path: Option[String],
      visibleTrack: VisibleTrack,
      secretParam: Option[String]
  ): Option[String] = {
    path.flatMap(p => {
      if (!visibleTrack.public && secretParam.isDefined) {
        val secret = URLEncoder.encode(secretParam.get, "UTF-8")
        Some(s"$p/$secret")
      } else {
        Some(p)
      }
    })

  }

  private def getDownloadable(visibleTrack: VisibleTrack, downloadsPerTrack: Option[Int], counts: StitchCounts) = {
    val trackDownloadable = visibleTrack.downloadable

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

  private def getPolicy(policy: ContentPolicy, client: Option[Urn]): Option[String] = {
    client.flatMap(urn =>
      if (AllowlistedClients.clients.contains(urn)) Some(policy.name)
      else None
    )
  }

  private def getMonetizationModel(monetizationModel: MonetizationModel, client: Option[Urn]): Option[String] = {
    client.flatMap(urn =>
      if (AllowlistedClients.clients.contains(urn)) Some(monetizationModel.name)
      else None
    )
  }
  private def getStreamUrl(visibleTrack: VisibleTrack): Option[String] = {
    if (visibleTrack.access.contains(Access.Blocked))
      None
    else
      urlFor(visibleTrack, "stream", getSecretTokenForPrivateTrack(visibleTrack))
  }

}
