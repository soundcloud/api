package com.soudcloud.authorization

import scala.collection.mutable.ListBuffer
import scala.util.Failure
import scala.util.Success
import scala.util.Try

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.json.Json

import play.api.libs.json.JsObject
import play.api.libs.json.JsValue

object CollectTrackUrns {

  def apply(content: String): Option[(TracksVisitor, List[Urn])] =
    Try(Json.fromJson(content)) match {
      case Success(json) => collectUrns(new TracksVisitor(json))
      case Failure(_) => None
    }

  private def collectUrns(visitor: TracksVisitor) =
    extractUrns(visitor) match {
      case Nil =>
        None
      case urns =>
        Some(visitor, urns)
    }

  private def extractUrns(visitor: TracksVisitor) = {
    val urns = ListBuffer[Urn]()
    visitor.apply {
      case (urn, track) =>
        urns += urn
        Some(track)
    }
    urns.toList
  }
}
