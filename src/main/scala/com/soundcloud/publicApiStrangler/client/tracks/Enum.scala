package com.soundcloud.publicApiStrangler.client.tracks

import play.api.libs.json._

abstract class EnumValue[E <: EnumValue[E]](val stringValue: String)

abstract class Enum[E <: EnumValue[E]: Manifest] {
  def all: Seq[E]

  implicit val format: Format[E] = new Format[E] {
    def reads(json: JsValue): JsResult[E] = {
      json.asOpt[String].flatMap(s => all.find(_.stringValue == s)) match {
        case Some(value) => JsSuccess(value)
        case None => JsError(s"Could not read ${json} into enum value of type ${manifest[E]}.")
      }
    }

    override def writes(enumValue: E): JsValue = {
      JsString(enumValue.stringValue)
    }
  }
}
