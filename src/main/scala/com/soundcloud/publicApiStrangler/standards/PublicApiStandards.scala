package com.soundcloud.publicApiStrangler.standards

import com.twitter.util.{Time, TimeFormat}
import play.api.libs.json.{JsString, Writes}

object PublicApiStandards {
  val timeFormat = new TimeFormat("yyyy/MM/dd hh:mm:ss ZZZZ")

  implicit val timeWrites: Writes[Time] = Writes(timeFormat.format _ andThen JsString)
}
