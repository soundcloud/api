package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  ContentRestriction,
  MonetizationModel,
  Reason
}
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.joda.time.LocalDateTime

trait TrackRepresentationsSpecificationContext extends UnitSpecification {

  trait TrackRepresentationsContext extends Scope {
    val trackUrn = Urn("soundcloud", "tracks", "987")
    val requestingUserUrn = Urn("soundcloud", "users", "112")
    val labelUrn = Urn("soundcloud", "users", "678")
    val trackOwnerUrn = Urn("soundcloud", "users", "3000")
    val createdAt = new LocalDateTime(2016, 5, 19, 18, 3, 4)
    val lastModified = new LocalDateTime(2016, 5, 20, 18, 3, 4)
    val authorization = new ContentAuthorization(
      trackUrn,
      ContentPolicy.MONETIZE,
      Reason.NOT_SUPPORTED,
      ContentRestriction.ENCRYPTED_STREAM_ONLY,
      MonetizationModel.AD_SUPPORTED
    )
    val geoblockingsList = List("DE", "FR")
    val domainLockingsList = List(
      DomainLocking(
        domain = "example.com",
        urn = Urn("soundcloud", "domain-lockings", "1"),
        trackUrn = Urn("soundcloud", "tracks", "123")
      )
    )
    val trackAudioMetadataList = TrackAudioMetadata(
      state = "failed",
      original_content_size = Some(9001),
      original_format = Some("vqf")
    )
    val trackRequest = TrackRequest(trackUrn, None)

    def trackOwner =
      User(
        urn = trackOwnerUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    def requestingUser =
      User(
        urn = requestingUserUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    def label =
      User(
        urn = labelUrn,
        permalink = "raz",
        username = "Raz Putin",
        avatar_url = "http://example.com/raz.jpg",
        permalink_url = "https://soundcloud.com/raz",
        city = None,
        country = None,
        tracks_count = 4,
        followers_count = Some(10000),
        followings_count = Some(10),
        verified = true,
        description = Some("Psychonaut Music Inc."),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    def geoblockings: Map[Urn, Geoblockings] = Map(trackUrn -> geoblockingsList)

    def domainLockings: Map[Urn, List[DomainLocking]] = Map(trackUrn -> domainLockingsList)

    def trackAudioMetadata: Map[Urn, TrackAudioMetadata] = Map(trackUrn -> trackAudioMetadataList)

    def trackvisibilityTrack(
        disabledAt: Option[LocalDateTime] = None,
        isPublic: Boolean = true,
        secretToken: String = "secr3t-Token",
        isDownloadable: Boolean = false,
        user: Urn = trackOwnerUrn,
        labelId: Option[Long] = Some(labelUrn.identifier.toLong),
        revealStats: Boolean = false,
        revealComments: Boolean = true
    ) =
      VisibleTrack(
        urn = trackUrn,
        userUrn = user,
        commentable = false,
        description = None,
        createdAt = createdAt,
        disabledAt = disabledAt,
        downloadable = isDownloadable,
        duration = 0,
        genre = None,
        lastModified = lastModified,
        permalink = null,
        permalinkUrl = None,
        public = isPublic,
        secretToken = Some(secretToken),
        userTags = List.empty,
        machineTags = List.empty,
        title = null,
        uid = Some("a1b2c3"),
        apiStreamable = None,
        streamable = true,
        revealStats = revealStats,
        revealComments = revealComments,
        labelName = None,
        license = null,
        embeddable = None,
        releaseYear = None,
        releaseMonth = None,
        releaseDay = None,
        embeddableBy = EmbeddingPermission.None,
        releaseDate = None,
        artwork = Artwork(None),
        publishedAt = None,
        purchaseUrl = Some("http://example.com/buy/7890"),
        purchaseTitle = Some("buy me pls"),
        bpm = Some(120.7),
        trackType = Some("original"),
        release = Some("DR012"),
        keySignature = Some("Emaj"),
        videoUrl = Some("http://example.com/video.mp4"),
        labelId = labelId,
        supplyChainStatus = None,
        waveformUrls = List.empty,
        transcodings = List.empty,
        authorization = authorization
      )

    def isrc(wrapped: String = "US-S1Z-99-00001"): Map[Urn, Isrc] =
      Map(trackUrn -> Isrc(wrapped))

    def stitchCounts: Map[Urn, StitchCounts] =
      Map(trackUrn -> StitchCounts(111, 222, 333, 444, 555))

    def userLikedTracks: Map[Urn, Boolean] =
      Map(trackUrn -> true)

    def waveformUrl(uid: String) =
      TrackWaveformUrl(uid, Url("https://bar.sndcdn.com/stream/a1b2c3.png"))
  }
}
