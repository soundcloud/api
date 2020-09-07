package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json._

import scala.annotation.unchecked.uncheckedVariance

sealed trait NonNullableValue[+A] {
  def toOptionalJsValue(implicit writes: Writes[A @uncheckedVariance]): Option[JsValue] = this match {
    case NonNullValue(value) => Some(Json.toJson(value))
    case NonNullMissingValue => None
  }
}

case class NonNullValue[+A](a: A) extends NonNullableValue[A]
case object NonNullMissingValue extends NonNullableValue[Nothing]

object NonNullableValue {
  def read[A](json: JsLookupResult)(implicit r: Reads[A]): NonNullableValue[A] = json match {
    case JsUndefined() | JsDefined(JsNull) => NonNullMissingValue
    case JsDefined(x) => NonNullValue(x.as[A])
  }
}
