package com.soudcloud.data

import com.soundcloud.scalakit.json.Json

import scala.xml.XML

object Parser {
  def apply(raw: String): ParsedValue = {
    if(isXml(raw)) {
      new XmlValue(XML.loadString(raw))
    } else {
      new JsonValue(Json.fromJson(raw))
    }
  }

  private def isXml(raw: String) = raw.startsWith("<")
}
