package com.soundcloud.apipublic.handler.support.requestParser

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.apipublic.client.mothership.request.representation.{
  MissingValue,
  NonNullMissingValue,
  NonNullValue,
  Value
}
import com.soundcloud.apipublic.handler.support.requestParser.TrackMetadataRequest.{
  getEmbeddable,
  parseBooleanInput,
  parseNullableStringInput
}
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.util.{Success, Try}

case class TrackMetadataUpdateRequest(track: TrackMetadataUpdates) extends TrackMetadataRequest {
  def getTrack: TrackMetadataUpdates = track
}

object TrackMetadataUpdateRequest {
  def fromForm(params: Map[String, String]): Outcome[TrackMetadataUpdateRequest] = {
    for {
      api_streamable <- parseBooleanInput(params, "streamable")
      downloadable <- parseBooleanInput(params, "downloadable")
      commentable <- parseBooleanInput(params, "commentable")
      reveal_stats <- parseBooleanInput(params, "reveal_stats")
      reveal_comments <- parseBooleanInput(params, "reveal_comments")

      track = new TrackMetadataUpdates(
        embeddable = getEmbeddable(params.get("embeddable_by")),
        description = parseNullableStringInput(params, "description"),
        genre = parseNullableStringInput(params, "genre"),
        isrc = parseNullableStringInput(params, "isrc"),
        artist = parseNullableStringInput(params, "metadata_artist"),
        label_name = parseNullableStringInput(params, "label_name"),
        license = parseNullableStringInput(params, "license"),
        purchase_url = parseNullableStringInput(params, "purchase_url"),
        release = parseNullableStringInput(params, "release"),
        release_date = parseNullableStringInput(params, "release_date"),
        sharing = parseNullableStringInput(params, "sharing"),
        tag_list = parseNullableStringInput(params, "tag_list"),
        purchase_title = parseNullableStringInput(params, "purchase_title"),
        title = params.get("title").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
        permalink = params.get("permalink").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
        geo_blockings =
          params.get("geo_blockings").map(v => Value[List[String]](v.split(",").toList)).getOrElse(MissingValue),
        api_streamable = api_streamable,
        downloadable = downloadable,
        commentable = commentable,
        reveal_stats = reveal_stats,
        reveal_comments = reveal_comments
      )
    } yield TrackMetadataUpdateRequest(track)
  }

  implicit val reads = Reads[TrackMetadataUpdateRequest] { json =>
    Try(
      TrackMetadataUpdateRequest(
        (json \ "track").as[TrackMetadataUpdates]
      )
    ) match {
      case Success(value) => JsSuccess(value)
      case _ => JsError("invalid track data")
    }
  }
}
