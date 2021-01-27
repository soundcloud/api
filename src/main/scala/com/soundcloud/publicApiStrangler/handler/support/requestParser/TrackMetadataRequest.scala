package com.soundcloud.publicApiStrangler.handler.support.requestParser

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
  def toBoolean(value: String): Boolean = {

    if (value != null) value.toLowerCase match {
      case "0" => false
      case "1" => true
      case "true" => true
      case "false" => false
      case _ => throw new IllegalArgumentException("For input string: \"" + value + "\"")
    }
    else
      throw new IllegalArgumentException("For input string: \"null\"")
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
