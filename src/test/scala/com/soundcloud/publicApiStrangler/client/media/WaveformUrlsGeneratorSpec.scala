package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class WaveformUrlsGeneratorSpec extends Specification with Mockito {
  trait Context extends Scope {
    val waveformUrlsGenerator = new WaveformUrlsGenerator("https://wave.invalid")
  }

  "#fromUid" >> {
    "returns the correct url" in new Context {
      waveformUrlsGenerator.fromUid("a_uid") ====
        TrackWaveformUrl(
          "a_uid",
          Url("https://wave.invalid/a_uid_m.png")
        )
    }
  }
}
