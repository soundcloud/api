package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json._

case class TrackUpdate(api_streamable: Option[Boolean],
                       commentable: Option[Boolean],
                       description: Option[String],
                       downloadable: Option[Boolean],
                       embeddable: Option[Boolean],
                       feedable: Option[Boolean],
                       genre: Option[String],
                       label_name: Option[String],
                       license: Option[String],
                       published_at: NullableValue[String],
                       permalink: String,
                       purchase_title: Option[String],
                       purchase_url: Option[String],
                       release_date: Option[String],
                       reveal_comments: Option[Boolean],
                       reveal_stats: Option[Boolean],
                       sharing: Option[String],
                       tag_list: Option[String],
                       title: String,
                       replacing_uid: Option[String],
                       replacing_original_filename: Option[String],
                       artwork_from_s3: NullableValue[S3Artwork])

object TrackUpdate {
  implicit val format = new Format[TrackUpdate] {
    override def writes(o: TrackUpdate) = {
      val baseJson = Json.obj(
        "api_streamable" -> Json.toJson(o.api_streamable),
        "commentable" -> Json.toJson(o.commentable),
        "description" -> Json.toJson(o.description),
        "downloadable" -> Json.toJson(o.downloadable),
        "embeddable" -> Json.toJson(o.embeddable),
        "feedable" -> Json.toJson(o.feedable),
        "genre" -> Json.toJson(o.genre),
        "label_name" -> Json.toJson(o.label_name),
        "license" -> Json.toJson(o.license),
        "permalink" -> Json.toJson(o.permalink),
        "purchase_title" -> Json.toJson(o.purchase_title),
        "purchase_url" -> Json.toJson(o.purchase_url),
        "release_date" -> Json.toJson(o.release_date),
        "reveal_comments" -> Json.toJson(o.reveal_comments),
        "reveal_stats" -> Json.toJson(o.reveal_stats),
        "sharing" -> Json.toJson(o.sharing),
        "tag_list" -> Json.toJson(o.tag_list),
        "title" -> Json.toJson(o.title),
        "replacing_uid" -> Json.toJson(o.replacing_uid),
        "replacing_original_filename" -> Json.toJson(o.replacing_original_filename))

      val artwork = o.artwork_from_s3.toOptionalJsValue.fold(Json.obj())(jsVal => Json.obj("artwork_from_s3" -> jsVal))
      val publishedAt = o.published_at.toOptionalJsValue.fold(Json.obj())(jsVal => Json.obj("published_at" -> jsVal))

      baseJson ++ artwork ++ publishedAt
    }

    override def reads(json: JsValue) = JsSuccess {
      TrackUpdate(
        api_streamable = (json \ "api_streamable").asOpt[Boolean],
        commentable = (json \ "commentable").asOpt[Boolean],
        description = (json \ "description").asOpt[String],
        downloadable = (json \ "downloadable").asOpt[Boolean],
        embeddable = (json \ "embeddable").asOpt[Boolean],
        feedable = (json \ "feedable").asOpt[Boolean],
        genre = (json \ "genre").asOpt[String],
        label_name = (json \ "label_name").asOpt[String],
        license = (json \ "license").asOpt[String],
        published_at = NullableValue.read[String](json \ "published_at"),
        permalink = (json \ "permalink").as[String],
        purchase_title = (json \ "purchase_title").asOpt[String],
        purchase_url = (json \ "purchase_url").asOpt[String],
        release_date = (json \ "release_date").asOpt[String],
        reveal_comments = (json \ "reveal_comments").asOpt[Boolean],
        reveal_stats = (json \ "reveal_stats").asOpt[Boolean],
        sharing = (json \ "sharing").asOpt[String],
        tag_list = (json \ "tag_list").asOpt[String],
        title = (json \ "title").as[String],
        replacing_uid = (json \ "replacing_uid").asOpt[String],
        replacing_original_filename = (json \ "replacing_original_filename").asOpt[String],
        artwork_from_s3 = NullableValue.read[S3Artwork](json \ "artwork_from_s3")
      )
    }
  }
}
