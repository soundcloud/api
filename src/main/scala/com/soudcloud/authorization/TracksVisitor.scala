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
  
  type T <: Track
  
  type VisitTrack = (Urn, T) => Option[T#T]
  
  def apply(visit: VisitTrack): Option[T#T]
  
  def wrapped: T#T
}

