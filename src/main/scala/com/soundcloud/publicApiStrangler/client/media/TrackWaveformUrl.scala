package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url

/**
  * Track waveform url.
  *
  * @param trackUid   Track uid.
  * @param pngUrl     Png waveform representation url.
  */
case class TrackWaveformUrl(trackUid: String, pngUrl: Url)
