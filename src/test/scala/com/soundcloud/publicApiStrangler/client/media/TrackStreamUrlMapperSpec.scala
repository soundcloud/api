package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.joda.time.DateTime
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf

class TrackStreamUrlMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val regularStreamsJson = withContentsOf("media-service", "stream_1")
    val previewStreamsJson = withContentsOf("media-service", "stream_snip_1")
    val waveformJson = withContentsOf("media-service", "waveform_urls")
    val mapper = new TrackStreamUrlMapper
  }

  "TrackStreamUrlMapper" should {

    "Give us Set of regular MediaUrl's for valid json." in new Context {
      val streams = mapper.map(regularStreamsJson)

      streams ==== Set(
        MediaUrl("hls_mp3_128_url", Url("https://ec-hls-media.soundcloud.com/playlist/2bAA4VpdwqqY.128.mp3/playlist.m3u8"), DateTime.parse("2014-08-05T14:09:28.887169799+02:00")),
        MediaUrl("rtmp_mp3_128_url", Url("rtmp://ec-rtmp-media.soundcloud.com/mp3:2bAA4VpdwqqY.128"), DateTime.parse("2014-08-05T14:09:08.112191418+02:00")),
        MediaUrl("http_mp3_128_url", Url("https://ec-media.soundcloud.com/2bAA4VpdwqqY.128.mp3"), DateTime.parse("2014-08-05T14:09:28.887275936+02:00"))
      )
    }

    "Give us Set of preview MediaUrl's for valid json." in new Context {
      val streams = mapper.map(previewStreamsJson)
      val expiresAt = DateTime.parse("2014-08-05T14:09:38.112302473+02:00")

      streams ==== Set(
        MediaUrl("http_mp3_128_url", Url("https://ec-preview-media-staging.sndcdn.com/preview/0/20/019aJrjWRlng.128.mp3"), expiresAt),
        MediaUrl("hls_mp3_128_url", Url("https://ec-hls-media.soundcloud.com/playlist/0/20/2bAA4VpdwqqY.128.mp3/playlist.m3u8"), expiresAt)
      )
    }

    "Return empty collection when unexpected json input is provided." in new Context {
      val streams = mapper.map(waveformJson)

      streams ==== Set()
    }
  }

}
