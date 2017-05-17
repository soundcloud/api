package com.soundcloud.publicApiStrangler.media

import com.soundcloud.jvmkit.module.util.Url
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf

class TrackWaveformUrlMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val waveformJson = withContentsOf("media-service", "waveform_urls")
    val streamJson = withContentsOf("media-service", "stream_1")
    val mapper = new TrackWaveformUrlMapper
  }

  "TrackWaveformUrlMapper" should {

    "Provide urls for expected json input" in new Context {
      val urls = mapper.map(waveformJson)

      urls ==== Set(
        TrackWaveformUrl("019aJrjWRlng", Url("https://wis.sndcdn.com/019aJrjWRlng_m.json"), Url("https://w1.sndcdn.com/019aJrjWRlng_m.png"), "stream"),
        TrackWaveformUrl("019aJrjWRlng", Url("https://wave.sndcdn.com/019aJrjWRlng_m_p30.json"), Url("https://wave.sndcdn.com/019aJrjWRlng_m_p30.png"), "preview", Some(90000)),
        TrackWaveformUrl("ZhDz6S4VqGLr", Url("https://wis.sndcdn.com/ZhDz6S4VqGLr_m.json"), Url("https://w1.sndcdn.com/ZhDz6S4VqGLr_m.png"), "stream"),
        TrackWaveformUrl("ZhDz6S4VqGLr", Url("https://wave.sndcdn.com/ZhDz6S4VqGLr_m_p30.json"), Url("https://wave.sndcdn.com/ZhDz6S4VqGLr_m_p30.png"), "preview", Some(30000))
      )
    }

    "Return empty collection when unexpected json input is provided" in new Context {
      val urls = mapper.map(streamJson)
      urls mustEqual Set()
    }
  }


}
