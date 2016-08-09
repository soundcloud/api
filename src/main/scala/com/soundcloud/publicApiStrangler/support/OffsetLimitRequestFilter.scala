package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.twitter.finagle.http.Request
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

class OffsetLimitRequestFilter(paths: Seq[String], maxOffset: Int) extends SimpleFilter[Request, RouterResponse] {
  override def apply(request: Request, next: Service[Request, RouterResponse]) = {
    (paths.exists(request.path.matches), optStringToOptInt(request.params.get("offset"))) match {
      case (true, Some(offset)) if offset > maxOffset => denial
      case _ => next(request)
    }
  }

  private def denial = Future.value(RouterResponse(mimicMotherShipBadRequestResponseBuilder.build, null))

  private def optStringToOptInt(string: Option[String]) = {
    string.map(_.trim).filter(!_.isEmpty).filter(_.forall(_.isDigit)).map(_.toInt)
  }

  private lazy val mimicMotherShipBadRequestResponseBuilder = {
    new ResponseBuilder().
      forbidden.
      header("Status", "403 Forbidden").
      header("Content-Type", "application/json; charset=utf-8").
      header("Date", DateTime.now.toString(DateTimeFormat.forPattern("E, d MMM yyyy HH:mm:ss z"))).
      body(Json.stringify(JsObject(Seq("errors" -> JsArray(Seq(JsObject(Seq("error_message" -> JsString("403 - Forbidden")))))))))
  }
}
