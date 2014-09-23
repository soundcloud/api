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
      case json: JsObject if (isTrack(json)) =>
        visitTrack(json, visit)
      case json: JsObject if (isStreamEntryWithTrack(json)) =>
        visitStreamEntryWithTrack(json, visit)
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


  private def isStreamEntryWithTrack(json: JsObject) = {
    val uuid : Option[String] = (json \ "uuid").asOpt[String]
    val createdAt : Option[String] = (json \ "created_at").asOpt[String]
    val track : Option[JsObject] = (json \ "track").asOpt[JsObject]
    uuid != None && createdAt != None && track != None && isTrack(track.get)
  }

  private def visitStreamEntryWithTrack(json: JsObject, visit: VisitTrack) = {

    val track : Option[JsObject] = (json \ "track").asOpt[JsObject]
    val id = (track.get \ "id").as[Int]
    val urn = Urn(s"soundcloud:tracks:$id")
    val result : Option[TrackType#Content] = visit(urn, JsonTrack(track.get))
    if (!result.isEmpty)
    {
      json
    }
    result

  }

}
