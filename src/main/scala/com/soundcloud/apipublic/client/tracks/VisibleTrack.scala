package com.soundcloud.apipublic.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.authorization.policies._
import org.joda.time.LocalDateTime

case class VisibleTrackCounts(
    plays: Option[Long],
    likes: Option[Long],
    reposts: Option[Long],
    comments: Option[Long],
    downloads: Option[Long]
)

case class VisibleTrack(
    urn: Urn,
    userUrn: Urn,
    uid: Option[String],
    title: String,
    createdAt: LocalDateTime,
    disabledAt: Option[LocalDateTime],
    downloadable: Boolean,
    duration: Int,
    commentable: Boolean,
    genre: Option[String],
    public: Boolean,
    permalinkUrl: Option[String],
    userTags: List[String],
    description: Option[String],
    secretToken: Option[String],
    revealStats: Boolean,
    artwork: Artwork,
    publishedAt: Option[LocalDateTime],
    machineTags: List[String],
    streamable: Boolean,
    apiStreamable: Option[Boolean],
    revealComments: Boolean,
    labelName: Option[String],
    license: String,
    embeddable: Option[Boolean],
    releaseYear: Option[Int],
    releaseMonth: Option[Int],
    releaseDay: Option[Int],
    embeddableBy: EmbeddingPermission,
    releaseDate: Option[LocalDateTime],
    purchaseUrl: Option[String],
    purchaseTitle: Option[String],
    authorization: ContentAuthorization,
    transcodings: List[Transcoding],
    supplyChainStatus: Option[String] = None,
    waveformUrls: List[WaveformUrl],
    bpm: Option[Double],
    release: Option[String],
    keySignature: Option[String],
    access: Option[Access],
    counts: VisibleTrackCounts,
    isrc: Option[String]
)
