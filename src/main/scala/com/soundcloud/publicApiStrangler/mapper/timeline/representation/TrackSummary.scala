package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.{JsonMapping, MappingContext}
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.mapper.timeline.EntitySummaryMapper
import play.api.libs.json.JsValue

class TrackSummary(jsonValue: JsValue, baseUrl: String, entitySummaryMapper: EntitySummaryMapper)(
    implicit context: MappingContext
) extends JsonMapping(jsonValue)
    with UrnSupport {
  val id = urn.identifier.toInt
  val kind = "track"
  val created_at = (json \ "created_at").asOpt[String]
  val last_modified = (json \ "last_modified").asOpt[String]
  val permalink = (json \ "permalink").asOpt[String]
  val permalink_url = (json \ "permalink_url").asOpt[String]
  val title = (json \ "title").asOpt[String]
  val duration = (json \ "duration").asOpt[Int]
  val sharing = (json \ "sharing").asOpt[String]
  // Option[Any] so we can override with an embedded attribute when calling WaveformMapper
  val waveform_url: Option[Any] = (json \ "waveform_url").asOpt[String]
  val stream_url = s"$baseUrl/tracks/$id/stream" // (json \ "stream_url").asOpt[String] // original url not supported by android
  val uri = s"$baseUrl/tracks/$id"
  val user_id = userId
  val user_uri = s"$baseUrl/users/$userId"

  private def userId: Integer =
    (json \ "user" \ "urn")
      .asOpt[Urn]
      .map(_.identifier.toInt: Integer)
      .orNull
}
