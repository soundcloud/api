package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Url

case class WaveformUrl(waveformType: WaveformType, json: Url, png: Url)

class WaveformType(s: String) {
  override def toString: String = s
}

object WaveformType {
  case object Full extends WaveformType("FULL")

  def parse(s: String): WaveformType = s match {
    case "FULL" => WaveformType.Full
    case _ => throw InvalidWaveformType(s)
  }

}

case class InvalidWaveformType(waveformType: String)
    extends RuntimeException(
      String.format(
        "[%s] is not a valid waveform type",
        waveformType
      )
    )
