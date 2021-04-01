package com.soundcloud.publicApiStrangler.service.tracks

import java.time.Instant
import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps._
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.tracks.{Artwork, EmbeddingPermission, WaveformType, WaveformUrl}
import org.joda.time.LocalDateTime
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.tracks.api.{
  Metadata,
  ContentAuthorization => ProtoContentAuthorization,
  Track => ProtoTrack,
  TrackType => ProtoTrackType,
  Transcoding => ProtoTranscoding,
  WaveformUrl => ProtoWaveformUrl
}

class VisibleTrackMapperSpec extends Specification {

  trait Context extends Scope {
    val trackUrn = Urn("soundcloud", "tracks", "432")
    val userUrn = Urn("soundcloud", "users", "123")
    val mapper = new VisibleTrackMapper()

    val protoTrack = ProtoTrack(
      metadata = Some(
        Metadata(
          urn = trackUrn.toString,
          userUrn = userUrn.toString,
          uid = None,
          title = "Some title",
          createdAt = Some(Instant.parse("2013-08-19T02:29:15.000Z").asProto),
          disabledAt = None,
          lastModified = Some(Instant.parse("2013-08-19T03:29:15.000Z").asProto),
          downloadable = false,
          duration = 123,
          commentable = true,
          genre = None,
          public = false,
          permalink = "lost-ii-by-dead-battery-dabin",
          permalinkUrl = Some(s"https://soundcloud.com/owner-perma/lost-ii-by-dead-battery-dabin"),
          userTags = List.empty,
          description = None,
          secretToken = None,
          revealStats = true,
          artwork = Some("dummy_artwork_filename-original.png"),
          publishedAt = None,
          machineTags = List.empty,
          streamable = true,
          apiStreamable = Some(true),
          revealComments = true,
          labelName = None,
          license = "all-rights-reserved",
          embeddable = None,
          releaseYear = None,
          releaseMonth = None,
          releaseDay = None,
          embeddableBy = "all",
          releaseDate = None,
          purchaseUrl = None,
          purchaseTitle = None,
          supplyChainStatus = Some("manual_upload"),
          bpm = None,
          trackType = ProtoTrackType.DEMO,
          release = None,
          keySignature = None,
          videoUrl = None,
          labelId = None
        )
      ),
      authorization = Some(
        ProtoContentAuthorization(
          policy = "ALLOW",
          reason = "DEFAULT",
          restrictions = Seq.empty,
          monetizationModel = "NOT_APPLICABLE",
          rulesetUuid = None
        )
      ),
      transcodings = Seq(
        ProtoTranscoding(
          uuid = "72590f2a-3351-11e8-b467-0ed5f89f718d",
          preset = "mp3_standard",
          mimeType = "audio/mpeg",
          protocols = List("hls", "encrypted-hls", "progressive"),
          protocolsSnippet = Seq("hls", "progressive"),
          quality = ProtoTranscoding.Quality.STANDARD,
          durationMs = 180000,
          durationSnippetMs = Some(30000),
          fileSize = Some(100)
        )
      ),
      waveformUrls = List(
        ProtoWaveformUrl(
          waveformType = "FULL",
          json = "https://wave.invalid/NnPYWvWwB6ln_m.json",
          png = "https://wave.invalid/NnPYWvWwB6ln_m.png"
        )
      )
    )
  }

  "apply" >> {
    "correctly maps a proto track to visible track" in new Context {
      val visibleTrack = mapper(protoTrack)
      visibleTrack.urn ==== trackUrn
      visibleTrack.userUrn ==== userUrn
      visibleTrack.uid ==== None
      visibleTrack.title ==== "Some title"
      visibleTrack.createdAt ==== new LocalDateTime(Instant.parse("2013-08-19T02:29:15.000Z").toEpochMilli)
      visibleTrack.disabledAt ==== None
      visibleTrack.lastModified ==== new LocalDateTime(Instant.parse("2013-08-19T03:29:15.000Z").toEpochMilli)
      visibleTrack.downloadable ==== false
      visibleTrack.duration ==== 123
      visibleTrack.commentable ==== true
      visibleTrack.genre ==== None
      visibleTrack.public ==== false
      visibleTrack.permalink ==== "lost-ii-by-dead-battery-dabin"
      visibleTrack.permalinkUrl ==== Some(s"https://soundcloud.com/owner-perma/lost-ii-by-dead-battery-dabin")
      visibleTrack.userTags ==== List.empty
      visibleTrack.description ==== None
      visibleTrack.secretToken ==== None
      visibleTrack.revealStats ==== true
      visibleTrack.artwork ==== Artwork(Some("dummy_artwork_filename-original.png"))
      visibleTrack.publishedAt ==== None
      visibleTrack.machineTags ==== List.empty
      visibleTrack.streamable ==== true
      visibleTrack.apiStreamable ==== Some(true)
      visibleTrack.revealComments ==== true
      visibleTrack.labelName ==== None
      visibleTrack.license ==== "all-rights-reserved"
      visibleTrack.embeddable == Some(true)
      visibleTrack.releaseYear ==== None
      visibleTrack.releaseMonth ==== None
      visibleTrack.releaseDay ==== None
      visibleTrack.embeddableBy ==== EmbeddingPermission.All
      visibleTrack.releaseDate ==== None
      visibleTrack.purchaseUrl ==== None
      visibleTrack.purchaseTitle ==== None
      visibleTrack.authorization ==== new ContentAuthorization(
        trackUrn,
        ContentPolicy.ALLOW,
        Reason.DEFAULT,
        Set.empty[ContentRestriction],
        MonetizationModel.NOT_APPLICABLE
      )
      visibleTrack.transcodings ==== List(
        Transcoding(
          uuid = "72590f2a-3351-11e8-b467-0ed5f89f718d",
          preset = "mp3_standard",
          mimeType = "audio/mpeg",
          protocols = List("hls", "encrypted-hls", "progressive"),
          protocolsSnippet = Some(List("hls", "progressive")),
          quality = "sq",
          durationMs = 180000,
          durationSnippetMs = Some(30000)
        )
      )
      visibleTrack.supplyChainStatus ==== Some("manual_upload")
      visibleTrack.waveformUrls ==== List(
        WaveformUrl(
          WaveformType.Full,
          Url("https://wave.invalid/NnPYWvWwB6ln_m.json"),
          Url("https://wave.invalid/NnPYWvWwB6ln_m.png")
        )
      )
      visibleTrack.bpm ==== None
      visibleTrack.trackType ==== Some("demo")
      visibleTrack.release ==== None
      visibleTrack.keySignature ==== None
      visibleTrack.videoUrl ==== None
      visibleTrack.labelId ==== None
    }

  }
}
