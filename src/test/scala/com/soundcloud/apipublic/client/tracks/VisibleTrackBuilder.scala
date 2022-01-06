package com.soundcloud.apipublic.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.authorization.policies.{Access, ContentAuthorization}
import com.soundcloud.apipublic.client.tracks.EmbeddingPermission.All
import org.joda.time.LocalDateTime

import scala.util.Random

class VisibleTrackBuilder {
  private val random = Random

  private var urn: Urn = Urn("soundcloud", "tracks", s"${random.nextLong()}")
  private var userUrn: Urn = Urn("soundcloud", "users", s"${random.nextLong()}")
  private var commentable: Boolean = true
  private var createdAt: LocalDateTime = LocalDateTime.parse("2013-08-19T02:29:15")
  private var description: Option[String] = None
  private var disabledAt: Option[LocalDateTime] = None
  private var downloadable: Boolean = false
  private var duration: Int = 123
  private var genre: Option[String] = None
  private var permalink: String = "lost-ii-by-dead-battery-dabin"
  private var permalinkUrl: Option[String] = Some(s"https://soundcloud.com/owner-perma/$permalink")
  private var public: Boolean = false
  private var secretToken: Option[String] = None
  private var userTags: List[String] = List.empty
  private var machineTags: List[String] = List.empty
  private var title: String = s"Some fairly ${random.nextString(5)} title"
  private var uid: Option[String] = None
  private var apiStreamable: Option[Boolean] = None
  private var streamable: Boolean = true
  private var revealComments: Boolean = true
  private var revealStats: Boolean = true
  private var labelName: Option[String] = None
  private var license: String = ""
  private var embeddable: Option[Boolean] = None
  private var releaseYear: Option[Int] = None
  private var releaseMonth: Option[Int] = None
  private var releaseDay: Option[Int] = None
  private var embeddableBy: EmbeddingPermission = All
  private var releaseDate: Option[LocalDateTime] = None
  private var artwork: Artwork = Artwork(Some("artwork"))
  private var publishedAt: Option[LocalDateTime] = None
  private var purchaseUrl: Option[String] = None
  private var purchaseTitle: Option[String] = None
  private var authorization: ContentAuthorization = new ContentAuthorizationBuilder().build
  private var transcodings: List[Transcoding] = List.empty
  private var supplyChainStatus: Option[String] = None
  private var waveformUrls: List[WaveformUrl] = List.empty
  private var bpm: Option[Double] = None
  private var release: Option[String] = None
  private var keySignature: Option[String] = None
  private var access: Option[Access] = None
  private var counts: VisibleTrackCounts = VisibleTrackCounts(None, None, None, None, None)
  private var isrc: Option[String] = None

  def setUrn(value: Urn) = {
    urn = value; this
  }

  def setUserUrn(value: Urn) = {
    userUrn = value; this
  }

  def setCommentable(value: Boolean) = {
    commentable = value; this
  }

  def setCreatedAt(value: LocalDateTime) = {
    createdAt = value; this
  }

  def setDescription(value: Option[String]) = {
    description = value; this
  }

  def setDisabledAt(value: Option[LocalDateTime]) = {
    disabledAt = value; this
  }

  def setDownloadable(value: Boolean) = {
    downloadable = value; this
  }

  def setDuration(value: Int) = {
    duration = value; this
  }

  def setGenre(value: Option[String]) = {
    genre = value; this
  }

  def setPermalink(value: String) = {
    permalink = value; this
  }

  def setPermalinkUrl(value: Option[String]) = {
    permalinkUrl = value; this
  }

  def setPublic(value: Boolean) = {
    public = value; this
  }

  def setSecretToken(value: Option[String]) = {
    secretToken = value; this
  }

  def setUserTags(value: List[String]) = {
    userTags = value; this
  }

  def setMachineTags(value: List[String]) = {
    machineTags = value; this
  }

  def setTitle(value: String) = {
    title = value; this
  }

  def setUid(value: Option[String]) = {
    uid = value; this
  }

  def setApiStreamable(value: Option[Boolean]) = {
    apiStreamable = value; this
  }

  def setStreamable(value: Boolean) = {
    streamable = value; this
  }

  def setRevealComments(value: Boolean) = {
    revealComments = value; this
  }

  def setRevealStats(value: Boolean) = {
    revealStats = value; this
  }

  def setLabelName(value: Option[String]) = {
    labelName = value; this
  }

  def setLicense(value: String) = {
    license = value; this
  }

  def setEmbeddable(value: Option[Boolean]) = {
    embeddable = value; this
  }

  def setReleaseYear(value: Option[Int]) = {
    releaseYear = value; this
  }

  def setReleaseMonth(value: Option[Int]) = {
    releaseMonth = value; this
  }

  def setReleaseDay(value: Option[Int]) = {
    releaseDay = value; this
  }

  def setEmbeddableBy(value: EmbeddingPermission) = {
    embeddableBy = value; this
  }

  def setReleaseDate(value: Option[LocalDateTime]) = {
    releaseDate = value; this
  }

  def setArtwork(value: Artwork) = {
    artwork = value; this
  }

  def setPublishedAt(value: Option[LocalDateTime]) = {
    publishedAt = value; this
  }

  def setPurchaseUrl(value: Option[String]) = {
    purchaseUrl = value; this
  }

  def setPurchaseTitle(value: Option[String]) = {
    purchaseTitle = value; this
  }

  def setAuthorization(value: ContentAuthorization) = {
    authorization = value; this
  }
  def setTranscodings(value: List[Transcoding]) = {
    transcodings = value; this
  }
  def setWaveformUrls(value: List[WaveformUrl]) = {
    waveformUrls = value; this
  }

  def setSupplyChainStatus(value: Option[String]) = {
    supplyChainStatus = value; this
  }

  def setBpm(value: Option[Double]) = {
    bpm = value; this
  }

  def setRelease(value: Option[String]) = {
    release = value; this
  }

  def setKeySignature(value: Option[String]) = {
    keySignature = value; this
  }

  def setAccess(value: Option[Access]) = {
    access = value; this
  }

  def setCounts(value: VisibleTrackCounts) = {
    counts = value; this
  }

  def setIsrc(value: Option[String]) = {
    isrc = value; this
  }

  def build: VisibleTrack =
    VisibleTrack(
      urn,
      userUrn,
      uid,
      title,
      createdAt,
      disabledAt,
      downloadable,
      duration,
      commentable,
      genre,
      public,
      permalinkUrl,
      userTags,
      description,
      secretToken,
      revealStats,
      artwork,
      publishedAt,
      machineTags,
      streamable,
      apiStreamable,
      revealComments,
      labelName,
      license,
      embeddable,
      releaseYear,
      releaseMonth,
      releaseDay,
      embeddableBy,
      releaseDate,
      purchaseUrl,
      purchaseTitle,
      authorization,
      transcodings,
      supplyChainStatus,
      waveformUrls,
      bpm,
      release,
      keySignature,
      access,
      counts,
      isrc
    )
}
