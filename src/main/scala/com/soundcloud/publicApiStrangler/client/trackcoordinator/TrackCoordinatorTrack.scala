package com.soundcloud.publicApiStrangler.client.trackcoordinator

import play.api.libs.json._
import play.api.libs.json.JsonNaming.SnakeCase

case class PublisherMetadata(isrc: Option[String])

object PublisherMetadata {
  private val snakeCase = Json.configured(JsonConfiguration(SnakeCase))
  implicit val format: Format[PublisherMetadata] = snakeCase.format
}

case class TrackCoordinatorTrack(
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
    publisher_metadata: Option[PublisherMetadata],
    label_name: Option[String],
    license: String,
    permalink: String,
    purchase_title: Option[String],
    purchase_url: Option[String],
    release_day: Option[Int],
    release_month: Option[Int],
    reveal_comments: Boolean,
    reveal_stats: Boolean,
    tag_list: Option[String],
    secret_token: Option[String], // not optional from track coordinator but without this CreatedTrack Writes does not compile :(
    uri: String
)

object TrackCoordinatorTrack {

  implicit val format = new Format[TrackCoordinatorTrack] {

    override def writes(o: TrackCoordinatorTrack) = {
      Json.obj(
        "urn" -> Json.toJson(o.urn),
        "public" -> Json.toJson(o.public),
        "title" -> Json.toJson(o.title),
        "api_streamable" -> Json.toJson(o.api_streamable),
        "commentable" -> Json.toJson(o.commentable),
        "description" -> Json.toJson(o.description),
        "downloadable" -> Json.toJson(o.downloadable),
        "embeddable" -> Json.toJson(o.embeddable),
        "genre" -> Json.toJson(o.genre),
        "geo_blockings" -> Json.toJson(o.geo_blockings),
        "publisher_metadata" -> Json.toJson(o.publisher_metadata),
        "label_name" -> Json.toJson(o.label_name),
        "license" -> Json.toJson(o.license),
        "permalink" -> Json.toJson(o.permalink),
        "purchase_title" -> Json.toJson(o.purchase_title),
        "purchase_url" -> Json.toJson(o.purchase_url),
        "release_day" -> Json.toJson(o.release_day),
        "release_month" -> Json.toJson(o.release_month),
        "reveal_comments" -> Json.toJson(o.reveal_comments),
        "reveal_stats" -> Json.toJson(o.reveal_stats),
        "tag_list" -> Json.toJson(o.tag_list),
        "secret_token" -> Json.toJson(o.secret_token),
        "uri" -> Json.toJson(o.uri)
      )
    }

    override def reads(json: JsValue): JsResult[TrackCoordinatorTrack] = JsSuccess {
      TrackCoordinatorTrack(
        urn = (json \ "urn").as[String],
        public = (json \ "public").as[Boolean],
        title = (json \ "title").as[String],
        api_streamable = (json \ "api_streamable").asOpt[Boolean],
        commentable = (json \ "commentable").as[Boolean],
        description = (json \ "description").asOpt[String],
        downloadable = (json \ "downloadable").asOpt[Boolean],
        embeddable = (json \ "embeddable").asOpt[Boolean],
        genre = (json \ "genre").asOpt[String],
        geo_blockings = (json \ "geo_blockings").asOpt[List[String]],
        publisher_metadata = (json \ "publisher_metadata").asOpt[PublisherMetadata],
        label_name = (json \ "label_name").asOpt[String],
        license = (json \ "license").as[String],
        permalink = (json \ "permalink").as[String],
        purchase_title = (json \ "purchase_title").asOpt[String],
        purchase_url = (json \ "purchase_url").asOpt[String],
        release_day = (json \ "release_day").asOpt[Int],
        release_month = (json \ "release_month").asOpt[Int],
        reveal_comments = (json \ "reveal_comments").as[Boolean],
        reveal_stats = (json \ "reveal_stats").as[Boolean],
        tag_list = (json \ "tag_list").asOpt[String],
        secret_token = (json \ "secret_token").asOpt[String],
        uri = (json \ "uri").as[String]
      )
    }
  }
}
