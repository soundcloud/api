package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url

/**
  * Track waveform url.
  *
  * @param trackUid   Track uid.
  * @param jsonUrl    Json waveform representation url.
  * @param pngUrl     Png waveform representation url.
  * @param label      Label for url. Is either 'stream' or 'preview'.
  * @param durationMs Optional duration in milliseconds. Default value = None.
  */
case class TrackWaveformUrl(trackUid: String, jsonUrl: Url, pngUrl: Url, label: String, durationMs: Option[Int] = None) {

  /**
    * Indicates if this track waveform represents a preview or not.
    *
    * @return true in case it is a preview, false otherwise.
    */
  def isPreview: Boolean = label.equals("preview")

}
