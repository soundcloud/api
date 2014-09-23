package com.soudcloud.authorization

import play.api.libs.json.JsValue
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import com.soundcloud.scalakit.Urn
import play.api.libs.json.JsString

class TracksJsonVisitor(val wrapped: JsValue) extends TracksVisitor {
  
  type TrackType = JsonTrack

  def apply(visit: VisitTrack): Option[JsValue] =
    apply(wrapped, visit)

  private def apply(json: JsValue, visit: VisitTrack): Option[JsValue] = {
    json match {
      case json: JsArray =>
        visitArray(json, visit)
      case json: JsObject if isTrack(json) =>
        visitTrack(json, visit)
      case json: JsObject =>
        visitObject(json, visit)
      case other =>
        Some(other)
    }
  }

  private def visitArray(json: JsArray, visit: VisitTrack) = {
    val items = json.as[List[JsValue]].map(apply(_, visit)).flatten
    Some(JsArray(items))
  }

  private def visitTrack(json: JsObject, visit: VisitTrack) = {
    val id = (json \ "id").as[Int]
    val urn = Urn(s"soundcloud:tracks:$id")
    visit(urn, JsonTrack(json))
  }

  private def visitObject(json: JsObject, visit: VisitTrack) =
    visitFields(json, visit).toList match {
      case Nil =>
        None
      case fields =>
        Some(JsObject(fields))
    }

  private def visitFields(json: JsObject, visit: VisitTrack) =
    json.fields.toMap.mapValues(apply(_, visit)).collect {
      case (name, Some(value)) =>
        name -> value
    }

  private def isTrack(json: JsObject) =
    json.fieldSet.contains(("kind", JsString("track")))

}
