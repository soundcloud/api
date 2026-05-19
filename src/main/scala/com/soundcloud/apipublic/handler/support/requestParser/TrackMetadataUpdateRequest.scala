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
import com.soundcloud.apipublic.support.MultipartParamsUtils
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.util.{Success, Try}

case class TrackMetadataUpdateRequest(track: TrackMetadataUpdates) extends TrackMetadataRequest {
  def getTrack: TrackMetadataUpdates = track
}

object TrackMetadataUpdateRequest {
  private val trackFormFieldPattern = """track\[(\S+)\]""".r

  private def normalizeFormParams(params: Map[String, String]): Map[String, String] =
    if (params.keys.exists(_.startsWith("track[")))
      MultipartParamsUtils.extractFieldsFromParams(
        trackFormFieldPattern,
        params.map { case (k, v) => k -> Seq(v) }
      )
    else params

  def fromForm(params: Map[String, String]): Outcome[TrackMetadataUpdateRequest] = {
    val normalizedParams = normalizeFormParams(params)
    for {
      api_streamable <- parseBooleanInput(normalizedParams, "streamable")
      downloadable <- parseBooleanInput(normalizedParams, "downloadable")
      commentable <- parseBooleanInput(normalizedParams, "commentable")
      reveal_stats <- parseBooleanInput(normalizedParams, "reveal_stats")
      reveal_comments <- parseBooleanInput(normalizedParams, "reveal_comments")

      track = new TrackMetadataUpdates(
        embeddable = getEmbeddable(normalizedParams.get("embeddable_by")),
        description = parseNullableStringInput(normalizedParams, "description"),
        genre = parseNullableStringInput(normalizedParams, "genre"),
        isrc = parseNullableStringInput(normalizedParams, "isrc"),
        artist = parseNullableStringInput(normalizedParams, "metadata_artist"),
        label_name = parseNullableStringInput(normalizedParams, "label_name"),
        license = parseNullableStringInput(normalizedParams, "license"),
        purchase_url = parseNullableStringInput(normalizedParams, "purchase_url"),
        release = parseNullableStringInput(normalizedParams, "release"),
        release_date = parseNullableStringInput(normalizedParams, "release_date"),
        sharing = parseNullableStringInput(normalizedParams, "sharing"),
        tag_list = parseNullableStringInput(normalizedParams, "tag_list"),
        purchase_title = parseNullableStringInput(normalizedParams, "purchase_title"),
        title = normalizedParams.get("title").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
        permalink = normalizedParams.get("permalink").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
        geo_blockings = normalizedParams
          .get("geo_blockings")
          .map(v => Value[List[String]](v.split(",").toList))
          .getOrElse(MissingValue),
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
