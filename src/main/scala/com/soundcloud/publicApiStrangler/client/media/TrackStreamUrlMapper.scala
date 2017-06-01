package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url
import org.joda.time.DateTime
import play.api.libs.json.JsValue


/**
  * Maps json response as returned by
  * [[https://github.com/soundcloud/media-service/tree/master/urlgen Media Service urlgen stream representation]]
  * (new format).
  */
class TrackStreamUrlMapper {
  def map(json: JsValue): Set[MediaUrl] = {
    val regularStreams = (json \ "available_streams").asOpt[Set[JsValue]]
    val previewStreams = (json \ "preview").asOpt[Set[JsValue]]

    val streams = (regularStreams, previewStreams) match {
      case (Some(s), _) => s
      case (None, Some(s)) => s
      case (None, None) => Set()
    }

    streams.map(jsValue => makeMediaUrl(jsValue))
  }

  private def makeMediaUrl(urlJson: JsValue): MediaUrl = {
    val protocol = (urlJson \ "protocol").as[String]
    val codec = (urlJson \ "codec").as[String]
    val bitrate = (urlJson \ "bitrate_kbps").as[Int]
    val name = "%s_%s_%d_url".format(protocol, codec, bitrate)
    val url = Url((urlJson \ "url").as[String])
    val expiresAt = DateTime.parse((urlJson \ "expires_at").as[String])
    MediaUrl(name, url, expiresAt)
  }

}

