package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.bff.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.authorization.TrackWaveformActionStatus.TrackWaveformActionStatus
import com.soundcloud.scalakit.Urn

/**
 * Indicates if for a given track urn we have to take action for replacing the waveform url.
 *
 * @param urn Track urn.
 * @param status Action.
 * @param url Optional track waveform url.
 */
case class TrackWaveformAction(urn:Urn, status:TrackWaveformActionStatus, url:Option[TrackWaveformUrl])
