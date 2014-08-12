package com.soudcloud.authorization

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.json.Json

object RenderContent {

  def apply[T <: Track](content: T#Content) =
    new ResponseBuilder().body(stringify(content))

  private def stringify(content: Track#Content) =
    content match {
      case json: JsonTrack#Content =>
        Json.stringify(json)
      case xml: XmlTrack#Content =>
        """<?xml version="1.0" encoding="UTF-8"?>\n""" + xml.toString
    }
}
