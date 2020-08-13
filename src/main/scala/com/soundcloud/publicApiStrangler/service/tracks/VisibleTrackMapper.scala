package com.soundcloud.publicApiStrangler.service.tracks

import java.time.Instant

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.tracks._
import org.joda.time.LocalDateTime
import proto.soundcloud.tracks.api.Transcoding.Quality
import proto.soundcloud.tracks.api.{Track => ProtoTrack}

class VisibleTrackMapper {

  def apply(track: ProtoTrack): VisibleTrack = {
    VisibleTrack(
      urn = Urn.parse(track.urn).get,
      userUrn = Urn.parse(track.userUrn).get,
      uid = track.uid,
      title = track.title,
      createdAt = new LocalDateTime(
        Instant
          .ofEpochSecond(track.createdAt.get.seconds, track.createdAt.get.nanos)
          .toEpochMilli
      ),
      disabledAt = track.disabledAt.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      lastModified = new LocalDateTime(
        Instant
          .ofEpochSecond(track.lastModified.get.seconds, track.lastModified.get.nanos)
          .toEpochMilli
      ),
      downloadable = track.downloadable,
      duration = track.duration.toInt,
      commentable = track.commentable,
      genre = track.genre,
      public = track.public,
      permalink = track.permalink,
      permalinkUrl = track.permalinkUrl,
      userTags = track.userTags.toList,
      description = track.description,
      secretToken = track.secretToken,
      revealStats = track.revealStats,
      artwork = Artwork(track.artwork),
      publishedAt = track.publishedAt.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      machineTags = track.machineTags.toList,
      streamable = track.streamable,
      apiStreamable = track.apiStreamable,
      revealComments = track.revealComments,
      labelName = track.labelName,
      license = track.license,
      embeddable = track.embeddable,
      releaseYear = track.releaseYear,
      releaseMonth = track.releaseMonth,
      releaseDay = track.releaseDay,
      embeddableBy = EmbeddingPermission.all.find(_.stringValue == track.embeddableBy).get,
      releaseDate = track.releaseDate.map(d =>
        new LocalDateTime(
          Instant
            .ofEpochSecond(d.seconds, d.nanos)
            .toEpochMilli
        )
      ),
      purchaseUrl = track.purchaseUrl,
      purchaseTitle = track.purchaseTitle,
      authorization = new ContentAuthorization(
        Urn.parse(track.urn).get,
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
      supplyChainStatus = track.supplyChainStatus,
      waveformUrls = track.waveformUrls
        .map(wfu => WaveformUrl(WaveformType.parse(wfu.waveformType), Url(wfu.json), Url(wfu.png)))
        .toList,
      bpm = track.bpm,
      trackType = track.trackType,
      release = track.release,
      keySignature = track.keySignature,
      videoUrl = track.videoUrl,
      labelId = track.labelId
    )
  }
}
