package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Geoblockings
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.tracks.{Artwork, EmbeddingPermission, TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.joda.time.LocalDateTime

trait TrackRepresentationsSpecificationContext extends UnitSpecification {

  trait TrackRepresentationsContext extends Scope {
    val trackUrn = Urn("soundcloud", "tracks", "987")
    val requestingUserUrn = Urn("soundcloud", "users", "112")
    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()
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

    val trackAudioMetadataList = TrackAudioMetadata(
      state = "failed",
      original_content_size = Some(9001),
      original_format = Some("vqf")
    )
    val trackRequest = TrackRequest(trackUrn, None)
    val trackRepresentationBuilder = new TrackRepresentationBuilder

    def trackOwner = new UserBuilder().setUrn(trackOwnerUrn).build

    def requestingUser = new UserBuilder().setUrn(requestingUserUrn).build

    def label = new UserBuilder().setUrn(labelUrn).build

    def geoblockings: Map[Urn, Geoblockings] = Map(trackUrn -> geoblockingsList)

    def trackAudioMetadata: Map[Urn, TrackAudioMetadata] = Map(trackUrn -> trackAudioMetadataList)

    def trackVisibilityTrack(
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
        permalink = "",
        permalinkUrl = None,
        public = isPublic,
        secretToken = Some(secretToken),
        userTags = List.empty,
        machineTags = List.empty,
        title = "",
        uid = Some("a1b2c3"),
        apiStreamable = None,
        streamable = true,
        revealStats = revealStats,
        revealComments = revealComments,
        labelName = None,
        license = "",
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
        authorization = authorization,
        access = Some(Access.Playable)
      )

    def isrc(wrapped: String = "US-S1Z-99-00001"): Map[Urn, Isrc] =
      Map(trackUrn -> Isrc(wrapped))

    def stitchCounts: Map[Urn, StitchCounts] =
      Map(trackUrn -> StitchCounts(111, 222, 333, 444, 555))

    def userLikedTracks: Map[Urn, Boolean] =
      Map(trackUrn -> true)

    def waveformUrl(uid: String) =
      TrackWaveformUrl(uid, Url("https://bar.sndcdn.com/stream/a1b2c3.png"))

    def createTrackRepresentation: TrackRepresentation =
      trackRepresentationBuilder.build(
        sessionUser = session.user,
        visibleTrack = trackVisibilityTrack(),
        user = trackOwner,
        isrc = Some(Isrc("US-S1Z-99-00001")),
        counts = StitchCounts(111, 222, 333, 444, 555),
        label = None,
        geoblockings = geoblockingsList,
        trackAudioMetadata = trackAudioMetadataList,
        isLiked = true,
        waveformUrl = waveformUrl(trackUrn.identifier),
        downloadsPerTrack = Some(0)
      )
  }
}
