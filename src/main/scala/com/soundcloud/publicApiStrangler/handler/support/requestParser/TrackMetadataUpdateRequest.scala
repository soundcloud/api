package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{
  MissingValue,
  NonNullMissingValue,
  NonNullValue,
  Value
}
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.util.{Success, Try}
import scala.util.control.NonFatal

case class TrackMetadataUpdateRequest(track: TrackMetadataUpdates)

object TrackMetadataUpdateRequest {
  implicit val reads = Reads[TrackMetadataUpdateRequest] { json =>
    Try(
      TrackMetadataUpdateRequest(
        track = (json \ "track").as[TrackMetadataUpdates]
      )
    ) match {
      case Success(value) => JsSuccess(value)
      case _ => JsError("invalid track data")
    }
  }

  def fromForm(params: Map[String, String]): Option[TrackMetadataUpdateRequest] = {
    try {
      Some(
        TrackMetadataUpdateRequest(
          track = new TrackMetadataUpdates(
            api_streamable = params.get("api_streamable").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            description = params.get("description").map(v => Value[String](v)).getOrElse(MissingValue),
            downloadable = params.get("downloadable").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            embeddable = params.get("embeddable").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            genre = params.get("genre").map(v => Value[String](v)).getOrElse(MissingValue),
            geo_blockings = params
              .get("geo_blockings")
              .map(v => Value[List[String]](v.split(",").toList))
              .getOrElse(MissingValue),
            isrc = params.get("isrc").map(v => Value[String](v)).getOrElse(MissingValue),
            label_name = params.get("label_name").map(v => Value[String](v)).getOrElse(MissingValue),
            license = params.get("license").map(v => Value[String](v)).getOrElse(MissingValue),
            permalink = params.get("permalink").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
            purchase_url = params.get("purchase_url").map(v => Value[String](v)).getOrElse(MissingValue),
            release_date = params.get("release_date").map(v => Value[String](v)).getOrElse(MissingValue),
            sharing = params.get("sharing").map(v => Value[String](v)).getOrElse(MissingValue),
            tag_list = params.get("tag_list").map(v => Value[String](v)).getOrElse(MissingValue),
            title = params.get("title").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
            commentable = params.get("commentable").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            reveal_stats = params.get("reveal_stats").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            reveal_comments =
              params.get("reveal_comments").map(v => Value[Boolean](v.toBoolean)).getOrElse(MissingValue),
            purchase_title = params.get("purchase_title").map(v => Value[String](v)).getOrElse(MissingValue)
          )
        )
      )
    } catch {
      case NonFatal(_) => None
    }
  }
}
