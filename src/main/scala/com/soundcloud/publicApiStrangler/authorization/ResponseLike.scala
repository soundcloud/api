package com.soundcloud.publicApiStrangler.authorization

import com.twitter.finagle.http._

trait ResponseLike[R] {

  def setCookie(r: R, key: String, value: String): Unit

  /**
    * Sets cookie. Not overriding values with same key.
    *
    * @param key   Key
    * @param value Value
    */
  def setCookieIfNotExists(r: R, key: String, value: String): Unit

  def setHeader(r: R, key: String, value: String): Unit

  /**
    * Sets header. Will not overwrite if an existing header already exists.
    *
    * @param r     Response
    * @param key   Header name.
    * @param value Header value.
    */
  def setHeaderIfNotExists(r: R, key: String, value: String): Unit

  def unauthorized: R

  def serviceUnavailableError: R

  def internalServerError: R

  def badRequest: R
}

object ResponseLike {
  def apply[R](implicit evidence: ResponseLike[R]) = evidence

  implicit val responseIsResponseLike = new ResponseLike[Response] {

    def setCookie(r: Response, key: String, value: String): Unit = r.cookies.add(key, new Cookie(key, value))

    def setCookieIfNotExists(r: Response, key: String, value: String): Unit =
      r.cookies.get(key) match {
        case None => setCookie(r, key, value)
        case Some(cookie) =>
      }

    def setHeader(r: Response, key: String, value: String): Unit = {
      r.headerMap.set(key, value)
      ()
    }

    def setHeaderIfNotExists(r: Response, key: String, value: String): Unit = {
      if (!r.headerMap.contains(key)) setHeader(r, key, value)
    }

    def unauthorized = Response(Status.Unauthorized)

    def serviceUnavailableError = Response(Status.ServiceUnavailable)

    def internalServerError = Response(Status.InternalServerError)

    def badRequest = Response(Status.BadRequest)

  }
}
