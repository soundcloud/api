package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, JsValue, Json}

sealed trait Result[+A] {
  def statusCode: Status
}

sealed trait Errors {
  def errors: Seq[Error]
}

case class Success[A](body: A, statusCode: Status = Status.Ok) extends Result[A]

case class Found(location: String) extends Result[String] {
  def statusCode = Status.Found
}

case object Forbidden extends Result[Nothing] {
  def statusCode = Status.Forbidden
}

case object Unauthorized extends Result[Nothing] {
  def statusCode = Status.Unauthorized
}

case object TooManyRequests extends Result[Nothing] {
  def statusCode = Status.TooManyRequests
}

case object PreconditionFailed extends Result[Nothing] {
  def statusCode = Status.PreconditionFailed
}

case class ServerError(errors: Seq[Error]) extends Result[Nothing] with Errors {
  def statusCode = Status.InternalServerError
}

case class BadRequest(errors: Seq[Error]) extends Result[Nothing] with Errors {
  def statusCode = Status.BadRequest
}

case class NotFound(errors: Seq[Error]) extends Result[Nothing] with Errors {
  def statusCode = Status.NotFound
}

case class UnprocessableEntity(errors: Seq[Error]) extends Result[Nothing] with Errors {
  def statusCode = Status.UnprocessableEntity
}

case class BadGateway(errors: Seq[Error]) extends Result[Nothing] with Errors {
  def statusCode = Status.BadGateway
}

case class Error(message: String, subject: Option[String])

case object Result {
  implicit val readError = Json.reads[Error]
  implicit val readBadRequest = Json.reads[BadRequest]

  def parseErrors(json: JsValue): Seq[Error] = {
    val errors = json.\("errors").asOpt[JsObject]
    errors
      .map {
        _.fields.map({ case (key, value) => Error(value.as[String], Some(key)) })
      }
      .getOrElse(Seq.empty)
  }
}
