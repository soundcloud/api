package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{
  MissingValue,
  NullValue,
  NullableValue,
  Value
}

trait TrackMetadataRequest {
  def getTrack: TrackMetadataUpdates
}

object TrackMetadataRequest {

  private def toBoolean(value: String): Option[Boolean] = {
    value.toLowerCase match {
      case "0" => Some(false)
      case "1" => Some(true)
      case "true" => Some(true)
      case "false" => Some(false)
      case _ => None
    }
  }

  def parseBooleanInput(params: Map[String, String], fieldName: String): Outcome[NullableValue[Boolean]] = {
    val param = params.get(fieldName)
    param match {
      case Some(value: String) =>
        toBoolean(value).map(Value[Boolean](_).good).getOrElse(NotValid(s"invalid ${fieldName} value").bad)
      case _ => MissingValue.good
    }
  }

  def parseNullableStringInput(params: Map[String, String], fieldName: String): NullableValue[String] = {
    params.get(fieldName).map(v => Value[String](v)).getOrElse(MissingValue)
  }

  def getEmbeddable(value: Option[String]): NullableValue[Boolean] = {
    value
      .map {
        case "all" => Value(true)
        case "me" => Value(false)
        case _ => NullValue
      }
      .getOrElse(MissingValue)
  }
}
