package com.soundcloud.publicApiStrangler.client.support

import com.twitter.finagle.http.Response

case class UnhandledResponseException(response: Response)
    extends Exception(s"${response.statusCode} : ${response.contentString}")

trait ResponseMapper[T] {
  def apply(response: Response): T
}
