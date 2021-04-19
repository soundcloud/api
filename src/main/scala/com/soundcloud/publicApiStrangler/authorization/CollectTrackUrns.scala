package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json

import scala.collection.mutable.ListBuffer

object CollectTrackUrns {
  def apply(content: String): Option[(TracksVisitor, List[Urn])] =
    visitorFor(content).map { visitor =>
      (visitor, extractUrns(visitor))
    }

  private def visitorFor(content: String) =
    if (hasTrack(content))
      Some(new TracksVisitor(Json.parse(content)))
    else
      None

  private def hasTrack(content: String) =
    content.indexOf("\"kind\":\"track\"") > 0

  private def extractUrns(visitor: TracksVisitor): List[Urn] = {
    val urns = ListBuffer[Urn]()
    visitor.apply {
      case (urn, track) =>
        urns += urn
        Some(track.json)
    }
    urns.toList
  }
}
