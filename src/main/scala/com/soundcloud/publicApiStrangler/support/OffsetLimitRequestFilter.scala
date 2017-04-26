package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

class OffsetLimitRequestFilter(paths: Seq[String], maxOffset: Int) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    (paths.exists(request.path.matches), optStringToOptInt(request.params.get("offset"))) match {
      case (true, Some(offset)) if offset > maxOffset => denial
      case _ => next(request)
    }
  }

  private def denial = Future.value(mimicMotherShipBadRequestResponseBuilder)

  private def optStringToOptInt(string: Option[String]) = {
    string.map(_.trim).filter(!_.isEmpty).filter(_.forall(_.isDigit)).map(_.toInt)
  }

  private lazy val mimicMotherShipBadRequestResponseBuilder = {
    new ResponseBuilder()
      .status(Status.Forbidden)
      .header("Status", "403 Forbidden")
      .header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z")))
      .body(Json.stringify(JsObject(Seq("errors" -> JsArray(Seq(JsObject(Seq("error_message" -> JsString("403 - Forbidden")))))))))
      .build
  }
}
