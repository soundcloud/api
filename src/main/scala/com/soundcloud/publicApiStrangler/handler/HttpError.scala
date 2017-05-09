package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.experimental.result.ErrorLike
import com.twitter.finagle.http.Status

case class HttpError(status: Status, private val message: Option[String]) extends ErrorLike {
  def description: String =
    message.getOrElse(s"${status.code} - ${com.twitter.finagle.http.Status(status.code).reason}")
}
