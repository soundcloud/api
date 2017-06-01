package com.soundcloud.publicApiStrangler.media

import com.soundcloud.jvmkit.module.util.Url
import play.api.libs.json.JsValue

/**
  * Maps json response as returned by
  * [[https://github.com/soundcloud/media-service/tree/master/urlgen Media Service urlgen waveform representation]].
  *
  */
class TrackWaveformUrlMapper {

  def map(json: JsValue): Set[TrackWaveformUrl] = {
    val trackEntries = (json \ "response").asOpt[Set[JsValue]]
    trackEntries match {
      case None => Set()
      case Some(someTrackEntries) => someTrackEntries.flatMap(trackEntry => mapTrack(trackEntry))
    }
  }

  private def mapTrack(trackEntry: JsValue): Set[TrackWaveformUrl] = {
    val trackUid = (trackEntry \ "uid").as[String]
    val urlEntries = (trackEntry \ "urls").asOpt[Set[JsValue]]
    urlEntries match {
      case None => Set()
      case Some(urls) => urls.map(url => mapUrl(trackUid, url))

    }
  }

  private def mapUrl(trackUid: String, urlEntry: JsValue): TrackWaveformUrl = {
    val label = (urlEntry \ "label").as[String]
    val json = (urlEntry \ "json").as[String]
    val png = (urlEntry \ "png").as[String]
    val durationMs = (urlEntry \ "duration_ms").asOpt[Int]
    TrackWaveformUrl(trackUid, Url(json), Url(png), label, durationMs)
  }

}
