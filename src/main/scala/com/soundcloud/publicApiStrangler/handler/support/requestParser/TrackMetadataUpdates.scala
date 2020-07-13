package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{NonNullableValue, NullableValue}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Json, Reads, Writes}

import scala.util.{Success, Try}

case class TrackMetadataUpdates(
    api_streamable: NullableValue[Boolean],
    description: NullableValue[String],
    downloadable: NullableValue[Boolean],
    embeddable: NullableValue[Boolean],
    genre: NullableValue[String],
    geo_blockings: NullableValue[List[String]],
    isrc: NullableValue[String],
    label_name: NullableValue[String],
    license: NullableValue[String],
    permalink: NonNullableValue[String],
    purchase_url: NullableValue[String],
    release_date: NullableValue[String],
    sharing: NullableValue[String],
    tag_list: NullableValue[String],
    title: NonNullableValue[String],
    commentable: NullableValue[Boolean],
    reveal_stats: NullableValue[Boolean],
    reveal_comments: NullableValue[Boolean],
    purchase_title: NullableValue[String]
)

object TrackMetadataUpdates {
  implicit val reads = Reads[TrackMetadataUpdates] { json =>
    Try(
      TrackMetadataUpdates(
        api_streamable = NullableValue.read[Boolean](json \ "api_streamable"),
        commentable = NullableValue.read[Boolean](json \ "commentable"),
        description = NullableValue.read[String](json \ "description"),
        downloadable = NullableValue.read[Boolean](json \ "downloadable"),
        embeddable = NullableValue.read[Boolean](json \ "embeddable"),
        genre = NullableValue.read[String](json \ "genre"),
        geo_blockings = NullableValue.read[List[String]](json \ "geo_blockings"),
        label_name = NullableValue.read[String](json \ "label_name"),
        license = NullableValue.read[String](json \ "license"),
        permalink = NonNullableValue.read[String](json \ "permalink"),
        purchase_title = NullableValue.read[String](json \ "purchase_title"),
        purchase_url = NullableValue.read[String](json \ "purchase_url"),
        release_date = NullableValue.read[String](json \ "release_date"),
        reveal_comments = NullableValue.read[Boolean](json \ "reveal_comments"),
        reveal_stats = NullableValue.read[Boolean](json \ "reveal_stats"),
        sharing = NullableValue.read[String](json \ "sharing"),
        tag_list = NullableValue.read[String](json \ "tag_list"),
        title = NonNullableValue.read[String](json \ "title"),
        isrc = NullableValue.read[String](json \ "isrc")
      )
    ) match {
      case Success(value) => JsSuccess(value)
      case _ => JsError("invalid track data")
    }
  }

  implicit val writes = Writes[TrackMetadataUpdates] { trackUpdate =>
    def getNullable[A](x: NullableValue[A], fieldName: String)(implicit writes: Writes[A]): JsObject = {
      x.toOptionalJsValue.fold(Json.obj())(jsVal => Json.obj(fieldName -> jsVal))
    }

    def getNonNullable[A](x: NonNullableValue[A], fieldName: String)(implicit writes: Writes[A]): JsObject = {
      x.toOptionalJsValue.fold(Json.obj())(jsVal => Json.obj(fieldName -> jsVal))
    }

    val nullableJsonVals =
      getNullable(trackUpdate.api_streamable, "api_streamable") ++
        getNullable(trackUpdate.commentable, "commentable") ++
        getNullable(trackUpdate.description, "description") ++
        getNullable(trackUpdate.downloadable, "downloadable") ++
        getNullable(trackUpdate.embeddable, "embeddable") ++
        getNullable(trackUpdate.genre, "genre") ++
        getNullable(trackUpdate.geo_blockings, "geo_blockings") ++
        getNullable(trackUpdate.license, "license") ++
        getNullable(trackUpdate.purchase_title, "purchase_title") ++
        getNullable(trackUpdate.release_date, "release_date") ++
        getNullable(trackUpdate.reveal_comments, "reveal_comments") ++
        getNullable(trackUpdate.tag_list, "tag_list") ++
        getNullable(trackUpdate.isrc, "isrc")

    val nonNullableJsonVals =
      getNonNullable(trackUpdate.title, "title") ++
        getNonNullable(trackUpdate.permalink, "permalink")

    nonNullableJsonVals ++ nullableJsonVals
  }
}
