package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{
  MissingValue,
  NonNullMissingValue,
  NonNullValue,
  Value
}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.TrackMetadataRequest.{getEmbeddable, toBoolean}
import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

import scala.util.{Success, Try}

case class TrackMetadataCreateRequest(track: TrackMetadataUpdates) extends TrackMetadataRequest {
  def getTrack: TrackMetadataUpdates = track
}

object TrackMetadataCreateRequest {
  val RequiredFields = Set("title") // throw error if title is not defined

  def containsRequiredFields(json: JsObject): Boolean = {
    RequiredFields.subsetOf(json.keys)
  }

  implicit val reads = Reads[TrackMetadataCreateRequest] { json =>
    Try(
      TrackMetadataCreateRequest(
        track = (json \ "track").as[TrackMetadataUpdates]
      )
    ) match {
      case Success(value) if containsRequiredFields((json \ "track").as[JsObject]) => JsSuccess(value)
      case _ => JsError("invalid track data")
    }
  }

  def fromForm(params: Map[String, String]): Option[TrackMetadataCreateRequest] = {
    val embeddable = getEmbeddable(
      params
        .get("embeddable_by")
    )

    Try(
      TrackMetadataCreateRequest(
        track =
          new TrackMetadataUpdates(
            api_streamable = params.get("streamable").map(v => Value[Boolean](toBoolean(v))).getOrElse(MissingValue),
            description = params.get("description").map(v => Value[String](v)).getOrElse(MissingValue),
            downloadable = params.get("downloadable").map(v => Value[Boolean](toBoolean(v))).getOrElse(MissingValue),
            embeddable = embeddable,
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
            release = params.get("release").map(v => Value[String](v)).getOrElse(MissingValue),
            release_date = params.get("release_date").map(v => Value[String](v)).getOrElse(MissingValue),
            sharing = params.get("sharing").map(v => Value[String](v)).getOrElse(MissingValue),
            tag_list = params.get("tag_list").map(v => Value[String](v)).getOrElse(MissingValue),
            title = params.get("title").map(v => NonNullValue[String](v)).get, // throw error if title is not defined
            commentable = params.get("commentable").map(v => Value[Boolean](toBoolean(v))).getOrElse(MissingValue),
            reveal_stats = params.get("reveal_stats").map(v => Value[Boolean](toBoolean(v))).getOrElse(MissingValue),
            reveal_comments =
              params.get("reveal_comments").map(v => Value[Boolean](toBoolean(v))).getOrElse(MissingValue),
            purchase_title = params.get("purchase_title").map(v => Value[String](v)).getOrElse(MissingValue)
          )
      )
    ).toOption
  }
}
