package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json._

sealed trait NullableValue[+A] {
  def toOptionalJsValue(implicit writes: Writes[A]): Option[JsValue] = this match {
    case Value(value) => Some(Json.toJson(value))
    case NullValue => Some(JsNull)
    case MissingValue => None
  }
}

case class Value[+A](a: A) extends NullableValue[A]

case object NullValue extends NullableValue[Nothing]

case object MissingValue extends NullableValue[Nothing]

object NullableValue {
  def read[A](json: JsLookupResult)(implicit r: Reads[A]): NullableValue[A] = json match {
    case JsUndefined() => MissingValue
    case JsDefined(JsNull) => NullValue
    case JsDefined(x) => Value(x.as[A])
  }
}
