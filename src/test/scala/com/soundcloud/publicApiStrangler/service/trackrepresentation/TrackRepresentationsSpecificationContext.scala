package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Geoblockings
import com.soundcloud.publicApiStrangler.client.tracks._
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

    val trackRequest = TrackRequest(trackUrn, None)

    def trackOwner = new UserBuilder().setUrn(trackOwnerUrn).build

    def requestingUser = new UserBuilder().setUrn(requestingUserUrn).build

    def label = new UserBuilder().setUrn(labelUrn).build

    def geoblockings: Map[Urn, Geoblockings] = Map(trackUrn -> geoblockingsList)

    def trackVisibilityTrack(
        disabledAt: Option[LocalDateTime] = None,
        isPublic: Boolean = true,
        secretToken: String = "secr3t-Token",
        isDownloadable: Boolean = false,
        user: Urn = trackOwnerUrn,
        revealStats: Boolean = false,
        revealComments: Boolean = true,
        isrc: Option[String] = Some("US-S1Z-99-00001")
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
        release = Some("DR012"),
        keySignature = Some("Emaj"),
        supplyChainStatus = None,
        waveformUrls = List.empty,
        transcodings = List.empty,
        authorization = authorization,
        access = Some(Access.Playable),
        counts = VisibleTrackCounts(Some(111), Some(222), Some(333), Some(444), Some(555)),
        isrc = isrc
      )

    def userLikedTracks: Map[Urn, Boolean] =
      Map(trackUrn -> true)

    def waveformUrl(uid: String) =
      TrackWaveformUrl(uid, Url("https://bar.sndcdn.com/stream/a1b2c3.png"))

    def createTrackRepresentation: TrackRepresentation =
      TrackRepresentationBuilder.fromVisibleTrack(
        client = session.agent,
        sessionUser = session.user,
        visibleTrack = trackVisibilityTrack(),
        user = trackOwner,
        geoblockings = geoblockingsList,
        isLiked = true,
        waveformUrl = waveformUrl(trackUrn.identifier)
      )
  }
}
