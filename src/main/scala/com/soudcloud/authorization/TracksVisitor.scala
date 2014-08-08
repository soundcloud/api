package com.soudcloud.authorization

import com.soundcloud.scalakit.Urn

import play.api.libs.json.JsArray
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

trait TracksVisitor {

  def apply(json: JsValue): Option[JsValue] = {
    json match {
      case json: JsArray =>
        visitArray(json)
      case json: JsObject if (isTrack(json)) =>
        visitTrack(json)
      case json: JsObject =>
        visitObject(json)
      case other =>
        Some(other)
    }
  }

  protected def visit(urn: Urn, track: JsObject): Option[JsObject]

  private def visitArray(json: JsArray) = {
    val items = json.as[List[JsValue]].map(apply(_)).flatten
    Some(JsArray(items))
  }

  private def visitTrack(json: JsObject) = {
    val id = (json \ "id").as[Int]
    val urn = Urn(s"soundcloud:tracks:$id")
    visit(urn, json)
  }

  private def visitObject(json: JsObject) =
    visitFields(json).toList match {
      case Nil =>
        None
      case fields =>
        Some(JsObject(fields))
    }

  private def visitFields(json: JsObject) =
    json.fields.toMap.mapValues(apply(_)).collect {
      case (name, Some(value)) =>
        name -> value
    }

  private def isTrack(json: JsObject) =
    json.fieldSet.contains(("kind", JsString("track")))
}
