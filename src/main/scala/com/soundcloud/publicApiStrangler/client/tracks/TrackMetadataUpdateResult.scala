package com.soundcloud.publicApiStrangler.client.tracks

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Format, Json, JsonConfiguration}

case class TrackMetadataUpdateResult(
    urn: String,
    public: Boolean,
    title: String,
    api_streamable: Option[Boolean],
    commentable: Boolean,
    description: Option[String],
    downloadable: Option[Boolean],
    embeddable: Option[Boolean],
    genre: Option[String],
    geo_blockings: Option[List[String]],
    isrc: Option[String],
    label_name: Option[String],
    license: String,
    permalink: String,
    purchase_title: Option[String],
    purchase_url: Option[String],
    release_day: Option[Int],
    release_month: Option[Int],
    reveal_comments: Boolean,
    reveal_stats: Boolean,
    tag_list: Option[String]
)

object TrackMetadataUpdateResult {
  private val snakeCase = Json.configured(JsonConfiguration(SnakeCase))

  implicit val format: Format[TrackMetadataUpdateResult] = snakeCase.format
}
