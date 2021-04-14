package com.soundcloud.publicApiStrangler.service.tracks

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.tracks._
import org.joda.time.LocalDateTime
import proto.soundcloud.tracks.api.Transcoding.Quality
import proto.soundcloud.tracks.api.{Track => ProtoTrack}

import java.time.Instant

class VisibleTrackMapper {

  def apply(track: ProtoTrack): VisibleTrack = {
    val metadata = track.metadata.get

    VisibleTrack(
      urn = Urn.parse(metadata.urn).get,
      userUrn = Urn.parse(metadata.userUrn).get,
      uid = metadata.uid,
      title = metadata.title,
      createdAt = new LocalDateTime(
        Instant
          .ofEpochSecond(metadata.createdAt.get.seconds, metadata.createdAt.get.nanos)
          .toEpochMilli
      ),
      disabledAt = metadata.disabledAt.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      lastModified = new LocalDateTime(
        Instant
          .ofEpochSecond(metadata.lastModified.get.seconds, metadata.lastModified.get.nanos)
          .toEpochMilli
      ),
      downloadable = metadata.downloadable,
      duration = metadata.duration.toInt,
      commentable = metadata.commentable,
      genre = metadata.genre,
      public = metadata.public,
      permalink = metadata.permalink,
      permalinkUrl = metadata.permalinkUrl,
      userTags = metadata.userTags.toList,
      description = metadata.description,
      secretToken = metadata.secretToken,
      revealStats = metadata.revealStats,
      artwork = Artwork(metadata.artwork),
      publishedAt = metadata.publishedAt.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      machineTags = metadata.machineTags.toList,
      streamable = metadata.streamable,
      apiStreamable = metadata.apiStreamable,
      revealComments = metadata.revealComments,
      labelName = metadata.labelName,
      license = metadata.license,
      embeddable = metadata.embeddable,
      releaseYear = metadata.releaseYear,
      releaseMonth = metadata.releaseMonth,
      releaseDay = metadata.releaseDay,
      embeddableBy = EmbeddingPermission.all.find(_.stringValue == metadata.embeddableBy).get,
      releaseDate = metadata.releaseDate.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      purchaseUrl = metadata.purchaseUrl,
      purchaseTitle = metadata.purchaseTitle,
      authorization = new ContentAuthorization(
        Urn.parse(metadata.urn).get,
        ContentPolicy.from(track.authorization.get.policy),
        Reason.from(track.authorization.get.reason),
        track.authorization.get.restrictions.map(ContentRestriction.from).toSet,
        MonetizationModel.from(track.authorization.get.monetizationModel)
      ),
      transcodings = track.transcodings.toList.map { transcoding =>
        Transcoding(
          uuid = transcoding.uuid,
          preset = transcoding.preset,
          mimeType = transcoding.mimeType,
          protocols = transcoding.protocols.toList,
          protocolsSnippet = Some(transcoding.protocolsSnippet.toList),
          quality = transcoding.quality match {
            case Quality.HIGH => "hq"
            case Quality.STANDARD => "sq"
            case _ => "unknown"
          },
          durationMs = transcoding.durationMs,
          durationSnippetMs = transcoding.durationSnippetMs
        )
      },
      supplyChainStatus = metadata.supplyChainStatus,
      waveformUrls = track.waveformUrls
        .map(wfu => WaveformUrl(WaveformType.parse(wfu.waveformType), Url(wfu.json), Url(wfu.png)))
        .toList,
      bpm = metadata.bpm,
      trackType = TrackType.fromProto(metadata.trackType).map(_.trackType),
      release = metadata.release,
      keySignature = metadata.keySignature,
      videoUrl = metadata.videoUrl,
      labelId = metadata.labelId,
      access = None
    )
  }
}
