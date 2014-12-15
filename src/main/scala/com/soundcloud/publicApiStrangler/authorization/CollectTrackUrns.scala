package com.soundcloud.publicApiStrangler.authorization

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import scala.xml.XML

object CollectTrackUrns {

  def apply(content: String): Option[(TracksVisitor, List[Urn])] =
    visitorFor(content).map {
      visitor => (visitor, extractUrns(visitor))
    }

  private def visitorFor(content: String) =
    if (hasJsonTrack(content))
      Some(new TracksJsonVisitor(Json.fromString(content)))
    else if (hasXmlTrack(content))
      Some(new TracksXmlVisitor(XML.loadString(content)))
    else
      None

  private def hasXmlTrack(content: String) =
    content.indexOf("<kind>track</kind>") > 0

  private def hasJsonTrack(content: String) =
    content.indexOf("\"kind\":\"track\"") > 0

  private def extractUrns(visitor: TracksVisitor): List[Urn] = {
    val urns = ListBuffer[Urn]()
    visitor.apply {
      case (urn, track) =>
        urns += urn
        Some(track.content)
    }
    urns.toList
  }
}
