package com.soundcloud.authorization

import com.soundcloud.scalakit.Urn

import play.api.libs.json.JsArray
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue

trait TracksVisitor {
  
  type TrackType <: Track
  
  type VisitTrack = (Urn, TrackType) => Option[TrackType#Content]
  
  def apply(visit: VisitTrack): Option[TrackType#Content]
  
  def wrapped: TrackType#Content
}

