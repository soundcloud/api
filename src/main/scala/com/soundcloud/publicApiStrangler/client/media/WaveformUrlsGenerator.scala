package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url

/**
  * Builds waveform urls for tracks.
  *
  * @param waveEndpoint
  */
class WaveformUrlsGenerator(waveEndpoint: String) {
  def fromUid(uid: String): TrackWaveformUrl =
    TrackWaveformUrl(uid, Url(s"$waveEndpoint/${uid}_m.png"))
}
