package com.soundcloud.apipublic.filter

import com.soundcloud.apipublic.support.ErrorResponse
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future

class OffsetLimitRequestFilter(paths: Seq[String], maxOffset: Int) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, next: Service[Request, Response]) = {
    (paths.exists(request.path.matches), optStringToOptInt(request.params.get("offset"))) match {
      case (true, Some(offset)) if offset > maxOffset => denial
      case _ => next(request)
    }
  }

  private def denial = Future.value(ErrorResponse.badRequest(s"Offset must be less than $maxOffset"))

  private def optStringToOptInt(string: Option[String]) = {
    string.map(_.trim).filter(_.nonEmpty).filter(_.forall(_.isDigit)).map(_.toInt)
  }
}
