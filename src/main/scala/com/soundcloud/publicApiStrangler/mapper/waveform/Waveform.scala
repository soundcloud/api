package com.soundcloud.publicApiStrangler.mapper.waveform

import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping

trait Waveform extends ObjectMapping[TrackWaveformUrl] {
  val pngUrl = resource.pngUrl.s
  val jsonUrl = resource.jsonUrl.s
  val duration = resource.durationMs
}