package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.AllowlistedClients
import com.soundcloud.publicApiStrangler.authorization.policies.{Access, ContentPolicy, MonetizationModel}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, UserRepresentation}
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.soundcloud.publicApiStrangler.client.tracks.{EmbeddingPermission, VisibleTrack}
import org.joda.time.DateTime

import java.net.URLEncoder
import scala.collection.immutable.HashSet

object TrackRepresentationBuilder {
  private val snippetDurationMs = 30000
  private val AbletonLiveApplication = Urn("soundcloud", "applications", "45176")
  private val cdnRoot = "https://i1.sndcdn.com"

  def fromTrackCoordinatorTrack(
      trackCoordinatorTrack: TrackCoordinatorTrack,
      user: UserRepresentation,
      agentUrn: Option[Urn]
  ): TrackRepresentation = {
    val urn = Urn.parse(trackCoordinatorTrack.urn).get
    val secretToken = getSecretTokenForPrivateTrack(trackCoordinatorTrack.public, trackCoordinatorTrack.secret_token)

    TrackRepresentation(
      urn = urn,
      createdAt = DateTime.parse(trackCoordinatorTrack.created_at, TrackRepresentation.dateTimeFormat).toLocalDateTime,
      duration = trackDuration(None, trackCoordinatorTrack.duration),
      public = trackCoordinatorTrack.public,
      userTags = trackCoordinatorTrack.tag_list,
      machineTags = None,
      apiStreamable = trackCoordinatorTrack.api_streamable,
      embeddableBy = EmbeddingPermission.all
        .find(_.stringValue == trackCoordinatorTrack.embeddable_by)
        .getOrElse(EmbeddingPermission.None),
      purchaseUrl = trackCoordinatorTrack.purchase_url,
      purchaseTitle = trackCoordinatorTrack.purchase_title,
      genre = trackCoordinatorTrack.genre,
      title = trackCoordinatorTrack.title,
      description = trackCoordinatorTrack.description,
      labelName = trackCoordinatorTrack.label_name,
      release = trackCoordinatorTrack.release,
      keySignature = trackCoordinatorTrack.key_signature,
      bpm = trackCoordinatorTrack.bpm.map(_.toDouble),
      releaseYear = trackCoordinatorTrack.release_year,
      license = trackCoordinatorTrack.license,
      access = None,
      commentable = trackCoordinatorTrack.commentable,
      user = user,
      isrc = trackCoordinatorTrack.isrc,
      availableCountries = getAvailableCountryNodes(trackCoordinatorTrack.geo_blockings.getOrElse(List.empty)),
      playbackCount = Some(trackCoordinatorTrack.playback_count),
      downloadCount = Some(trackCoordinatorTrack.downloads_count),
      favoritingsCount = Some(trackCoordinatorTrack.favoritings_count),
      repostsCount = None,
      releaseDay = trackCoordinatorTrack.release_day,
      releaseMonth = trackCoordinatorTrack.release_month,
      uri = urlFor(urn, trackCoordinatorTrack.public, secretToken),
      streamUrl = urlFor(urn, trackCoordinatorTrack.public, "stream", secretToken),
      downloadUrl = urlFor(urn, trackCoordinatorTrack.public, "download", secretToken),
      permalinkUrl =
        secretPath(Some(trackCoordinatorTrack.permalink_url), trackCoordinatorTrack.public, secretToken, agentUrn),
      secretUri = secretUrl(trackCoordinatorTrack.uri, trackCoordinatorTrack.public, secretToken),
      commentCount = Some(trackCoordinatorTrack.comment_count),
      userFavourite = Some(false),
      userPlaybackCount = Some(trackCoordinatorTrack.playback_count),
      waveformUrl = trackCoordinatorTrack.waveform_url,
      artworkUrl = trackCoordinatorTrack.artwork_url,
      downloadable = trackCoordinatorTrack.downloadable.getOrElse(false),
      policy = None,
      monetizationModel = None
    )
  }

  def fromVisibleTrack(
      client: Option[Urn],
      sessionUser: Option[Urn],
      visibleTrack: VisibleTrack,
      user: UserRepresentation,
      geoblockings: Geoblockings,
      isLiked: Boolean
  ): TrackRepresentation = {
    val isAnonymous = sessionUser.isEmpty
    val secretToken = getSecretTokenForPrivateTrack(visibleTrack.public, visibleTrack.secretToken)

    TrackRepresentation(
      urn = visibleTrack.urn,
      createdAt = visibleTrack.createdAt,
      duration = trackDuration(Option(visibleTrack.authorization.policy), visibleTrack.duration),
      public = visibleTrack.public,
      userTags = mkTagList(visibleTrack.userTags),
      machineTags = mkTagList(visibleTrack.machineTags),
      apiStreamable = visibleTrack.apiStreamable,
      embeddableBy = visibleTrack.embeddableBy,
      purchaseUrl = visibleTrack.purchaseUrl,
      purchaseTitle = visibleTrack.purchaseTitle,
      genre = visibleTrack.genre,
      title = visibleTrack.title,
      description = visibleTrack.description,
      labelName = visibleTrack.labelName,
      release = visibleTrack.release,
      keySignature = visibleTrack.keySignature,
      bpm = visibleTrack.bpm,
      releaseYear = visibleTrack.releaseYear,
      license = visibleTrack.license,
      access = visibleTrack.access,
      commentable = visibleTrack.commentable,
      user = user,
      isrc = visibleTrack.isrc,
      availableCountries = getAvailableCountryNodes(geoblockings),
      playbackCount = visibleTrack.counts.plays,
      downloadCount = visibleTrack.counts.downloads,
      favoritingsCount = visibleTrack.counts.likes,
      repostsCount = visibleTrack.counts.reposts,
      releaseDay = releaseDayFor(visibleTrack),
      releaseMonth = releaseMonthFor(visibleTrack),
      uri = urlFor(visibleTrack.urn, visibleTrack.public, secretToken),
      streamUrl = getStreamUrl(visibleTrack),
      downloadUrl = urlFor(visibleTrack.urn, visibleTrack.public, "download", secretToken),
      permalinkUrl = secretPath(visibleTrack.permalinkUrl, visibleTrack.public, secretToken),
      secretUri = getSecretUri(visibleTrack),
      commentCount = visibleTrack.counts.comments,
      userFavourite = if (!isAnonymous) Some(isLiked) else None,
      userPlaybackCount = if (!isAnonymous) Some(1) else None,
      waveformUrl = visibleTrack.waveformUrls.map(_.png.s).headOption.getOrElse(""),
      artworkUrl = visibleTrack.artwork.filename.map(imageUrl),
      downloadable = visibleTrack.downloadable,
      policy = getPolicy(visibleTrack.authorization.policy, client),
      monetizationModel = getMonetizationModel(visibleTrack.authorization.monetizationModel, client)
    )
  }
  private val baseUrl = "https://api.soundcloud.com/tracks"

  private def releaseDayFor(visibleTrack: VisibleTrack): Option[Int] =
    visibleTrack.releaseYear.map(_ => visibleTrack.releaseDay.getOrElse(1))

  private def releaseMonthFor(visibleTrack: VisibleTrack): Option[Int] =
    visibleTrack.releaseYear.map(_ => visibleTrack.releaseMonth.getOrElse(1))

  private def getSecretTokenForPrivateTrack(isPublic: Boolean, secretToken: Option[String]): Option[String] =
    if (!isPublic) secretToken else None

  private def getSecretUri(visibleTrack: VisibleTrack): Option[String] = {
    if (!visibleTrack.public)
      visibleTrack.secretToken.map(token =>
        s"https://api.soundcloud.com/tracks/${visibleTrack.urn.identifier}?secret_token=$token"
      )
    else None
  }

  private def imageUrl(imageFile: String): String = {
    val s3FilenamePattern = """(.*)-original\.\w*""".r

    imageFile match {
      case s3FilenamePattern(s3filename) => cdnRoot + s"/$s3filename-large.jpg"
      case _ => cdnRoot + "/" + imageFile
    }
  }

  def mkTagList(tagList: List[String]): Option[String] =
    Option(tagList.map(quoteTagIfNecessary).mkString(" "))

  private def quoteTagIfNecessary(tag: String): String =
    if (tag.exists(_.isSpaceChar))
      "\"" + tag + "\""
    else
      tag

  private def getAvailableCountryNodes(geoblockings: Geoblockings): Option[HashSet[String]] = {
    if (geoblockings.nonEmpty)
      Some(Country.officiallyAssignedAlpha2Codes.--(geoblockings))
    else None
  }

  private def urlFor(urn: Urn, isPublic: Boolean, subresource: String, secretParam: Option[String]) =
    secretUrl(s"$baseUrl/${urn.identifier.toLong}/$subresource", isPublic, secretParam)

  private def urlFor(urn: Urn, isPublic: Boolean, secretParam: Option[String]) =
    secretUrl(s"$baseUrl/${urn.identifier.toLong}", isPublic, secretParam)

  private def secretUrl(url: String, isPublic: Boolean, secretParam: Option[String]): Option[String] = {
    if (!isPublic && secretParam.isDefined) {
      val secret = URLEncoder.encode(secretParam.get, "UTF-8")
      Some(s"$url?secret_token=$secret")
    } else {
      Some(url)
    }
  }

  private def secretPath(
      path: Option[String],
      isPublic: Boolean,
      secretParam: Option[String],
      agentUrn: Option[Urn] = None
  ): Option[String] = {
    path.map(p => {
      if (agentUrn.contains(AbletonLiveApplication)) {
        // TODO: remove this workaround once better solution for Ableton integration is found: https://jira.soundcloud.org/browse/INT-279
        p
      } else if (!isPublic && secretParam.isDefined) {
        val secret = URLEncoder.encode(secretParam.get, "UTF-8")
        s"$p/$secret"
      } else {
        p
      }
    })
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

  private def trackDuration(policy: Option[ContentPolicy], duration: Int): Int = {
    val fullDuration = duration
    if (policy.contains(ContentPolicy.SNIP) && fullDuration > snippetDurationMs) {
      snippetDurationMs
    } else {
      fullDuration
    }
  }

  private def getStreamUrl(visibleTrack: VisibleTrack): Option[String] = {
    val secretToken = getSecretTokenForPrivateTrack(visibleTrack.public, visibleTrack.secretToken)
    if (visibleTrack.access.contains(Access.Blocked))
      None
    else {
      urlFor(visibleTrack.urn, visibleTrack.public, "stream", secretToken)
    }
  }

}
