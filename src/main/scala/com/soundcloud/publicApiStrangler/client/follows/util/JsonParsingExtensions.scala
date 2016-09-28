package com.soundcloud.publicApiStrangler.client.follows.util

import com.twitter.util.{Throw, Return, Try}
import org.joda.time.{DateTimeZone, DateTime, LocalDateTime}
import play.api.libs.json._

object JsonParsingExtensions {
  implicit val localDateTimeReads: Reads[LocalDateTime] = new Reads[LocalDateTime] {
    override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(value) =>
        Try(new LocalDateTime(new DateTime(value, DateTimeZone.UTC))).asJsResult
      case _ => JsError(s"Unknown json format for: $json")
    }
  }

  implicit class TryOps[T](val t: Try[T]) extends AnyVal {
    def asJsResult: JsResult[T] = t match {
      case Return(r) => JsSuccess(r)
      case Throw(e) => JsError(e.getMessage)
    }
  }
}
