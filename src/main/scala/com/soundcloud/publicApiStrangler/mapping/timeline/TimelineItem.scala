package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.JsonMapping


trait TimelineItem {
  self: JsonMapping =>

  val created_at = (json \ "timestamp").as[String]
  val `type` = typeFor((json \ "type").as[String])

  private def typeFor(timelineType: String) = {
    timelineType match {
      case "user:follow" => "affiliation"
      case "track:comment" => "comment"
      case other => timelineType.replace(":", "-")
    }
  }
}
