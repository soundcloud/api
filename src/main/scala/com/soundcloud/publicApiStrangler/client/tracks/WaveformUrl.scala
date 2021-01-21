package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Url
import play.api.libs.json._

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

  implicit val reads: Reads[WaveformType] = {
    case JsString(s) => JsSuccess(WaveformType.parse(s))
    case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("Could not parse waveformType"))))
  }
}

case class InvalidWaveformType(waveformType: String)
    extends RuntimeException(
      String.format(
        "[%s] is not a valid waveform type",
        waveformType
      )
    )
