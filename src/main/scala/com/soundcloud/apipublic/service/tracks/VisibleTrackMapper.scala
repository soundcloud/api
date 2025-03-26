package com.soundcloud.apipublic.service.tracks

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.apipublic.authorization.policies._
import com.soundcloud.apipublic.client.tracks._
import org.joda.time.{DateTimeZone, LocalDateTime}
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
          .toEpochMilli,
        DateTimeZone.UTC
      ),
      disabledAt = metadata.disabledAt.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli,
          DateTimeZone.UTC
        )
      ),
      downloadable = metadata.downloadable && track.downloadMetadata.forall(_.allowed),
      duration = metadata.duration.toInt,
      commentable = metadata.commentable,
      genre = metadata.genre,
      public = metadata.public,
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
            .toEpochMilli,
          DateTimeZone.UTC
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
            .toEpochMilli,
          DateTimeZone.UTC
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
      release = metadata.release,
      keySignature = metadata.keySignature,
      access = None,
      counts = VisibleTrackCounts(
        track.counts.flatMap(_.plays),
        track.counts.flatMap(_.likes),
        track.counts.flatMap(_.reposts),
        track.counts.flatMap(_.comments),
        track.downloadMetadata.flatMap(_.count)
      ),
      isrc = track.publisherMetadata.flatMap(_.isrc),
      metaDataArtist = track.publisherMetadata.flatMap(_.artist)
    )
  }
}
