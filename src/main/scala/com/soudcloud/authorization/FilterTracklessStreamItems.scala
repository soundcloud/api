package com.soudcloud.authorization

import play.api.libs.json.{JsArray, JsObject, JsValue}

object FilterTracklessStreamItems {

  def apply[T <: Track](content: T#Content) : T#Content =
    filter(content)

  private def filter[T <: Track](content: T#Content) : T#Content =
    content match {
      case json: JsonTrack#Content =>
        filterJson(json).asInstanceOf[T#Content]
      case xml: XmlTrack#Content =>
        xml.asInstanceOf[T#Content]
    }

  private def filterJson(json: JsonTrack#Content) : JsonTrack#Content = {
    if (isStream(json)) {
      removeMissingTracks(json)
    } else {
      json
    }
  }

  private def isStream(json: JsonTrack#Content) : Boolean =
    (json \ "collection").asOpt[List[JsValue]] match {
      case None =>
        false
      case Some(Nil) =>
        false
      case Some(head :: _) =>
        (head \ "uuid").asOpt[String].nonEmpty && (head \ "created_at").asOpt[String].nonEmpty
    }

  private def removeMissingTracks(json: JsonTrack#Content) : JsonTrack#Content = {
    val oldCollection = (json \ "collection").as[Seq[JsObject]]
    val newCollection = oldCollection.filter(hasTrack)
    json.as[JsObject] - "collection" + ("collection" -> JsArray(newCollection))
  }

  private def hasTrack(json : JsObject) = (json \ "track").asOpt[JsObject].nonEmpty

}
