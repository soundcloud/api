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

  def apply(content: String): Option[(JsValue, List[Urn])] =
    Try(Json.fromJson(content)) match {
      case Success(json) => collectUrns(json)
      case Failure(_) => None
    }

  private def collectUrns(json: JsValue) =
    extractUrns(json) match {
      case Nil =>
        None
      case urns =>
        Some(json, urns)
    }

  private def extractUrns(json: JsValue) = {
    val urns = ListBuffer[Urn]()
    urnsVisitor(urns).apply(json)
    urns.toList
  }

  private def urnsVisitor(urns: ListBuffer[Urn]) =
    new TracksVisitor {
      def visit(urn: Urn, track: JsObject) = {
        urns += urn
        Some(track)
      }
    }
}
