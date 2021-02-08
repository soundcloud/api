package com.soundcloud.publicApiStrangler.handler.support.requestParser

import cats.implicits._
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{
  MissingValue,
  NonNullMissingValue,
  NonNullValue,
  NullableValue,
  Value
}

import com.soundcloud.publicApiStrangler.handler.support.requestParser.TrackMetadataRequest.getEmbeddable
import play.api.libs.json.{JsError, JsObject, JsSuccess, Reads}

import scala.util.{Success, Try}

case class TrackMetadataCreateRequest(track: TrackMetadataUpdates) extends TrackMetadataRequest {
  def getTrack: TrackMetadataUpdates = track
}

object TrackMetadataCreateRequest {
  def fromForm(params: Map[String, String]): Outcome[TrackMetadataCreateRequest] = {
    for {
      api_streamable <- parseBooleanInput(params, "streamable")
      downloadable <- parseBooleanInput(params, "downloadable")
      commentable <- parseBooleanInput(params, "commentable")
      reveal_stats <- parseBooleanInput(params, "reveal_stats")
      reveal_comments <- parseBooleanInput(params, "reveal_comments")

      title <- params
        .get("title")
        .map(v => NonNullValue[String](v))
        .outcome
        .leftMap(_ => NotValid("title field is required"))

      track = new TrackMetadataUpdates(
        embeddable = getEmbeddable(params.get("embeddable_by")),
        description = parseNullableStringInput(params, "description"),
        genre = parseNullableStringInput(params, "genre"),
        isrc = parseNullableStringInput(params, "isrc"),
        label_name = parseNullableStringInput(params, "label_name"),
        license = parseNullableStringInput(params, "license"),
        purchase_url = parseNullableStringInput(params, "purchase_url"),
        release = parseNullableStringInput(params, "release"),
        release_date = parseNullableStringInput(params, "release_date"),
        sharing = parseNullableStringInput(params, "sharing"),
        tag_list = parseNullableStringInput(params, "tag_list"),
        purchase_title = parseNullableStringInput(params, "purchase_title"),
        geo_blockings =
          params.get("geo_blockings").map(v => Value[List[String]](v.split(",").toList)).getOrElse(MissingValue),
        permalink = params.get("permalink").map(v => NonNullValue[String](v)).getOrElse(NonNullMissingValue),
        api_streamable = api_streamable,
        downloadable = downloadable,
        title = title,
        commentable = commentable,
        reveal_stats = reveal_stats,
        reveal_comments = reveal_comments
      )
    } yield TrackMetadataCreateRequest(track)
  }

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

  def toBoolean(value: String): Outcome[Boolean] = {
    if (value != null) value.toLowerCase match {
      case "0" => false.good
      case "1" => true.good
      case "true" => true.good
      case "false" => false.good
      case _ => Bad(NotValid(""))
    }
    else Bad(NotValid(""))
  }

  private def parseBooleanInput(params: Map[String, String], fieldName: String): Outcome[NullableValue[Boolean]] = {
    params
      .get(fieldName)
      .map {
        toBoolean(_)
          .map(Value[Boolean])
          .leftMap(_ => NotValid(s"invalid ${fieldName} value"))
      }
      .getOrElse(MissingValue.good)
  }

  private def parseNullableStringInput(params: Map[String, String], fieldName: String): NullableValue[String] = {
    params.get(fieldName).map(v => Value[String](v)).getOrElse(MissingValue)
  }
}
