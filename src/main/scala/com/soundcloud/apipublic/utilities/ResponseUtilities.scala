package com.soundcloud.apipublic.utilities

import com.soundcloud.jvmkit.module.http.client.RetryFunction
import com.soundcloud.jvmkit.module.util.logging.SoundCloudLoggerFactory
import com.twitter.finagle.http.Response
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.finagle.{ChannelClosedException, Failure, TimeoutException}
import com.twitter.util.{Return, Throw, TimeoutException => UtilTimeoutException}
import org.slf4j.Logger

object ResponseUtilities {

  val logger: Logger = SoundCloudLoggerFactory.getLogger(getClass)

  private def isRetryableStatus(r: Response): Boolean =
    (r.statusCode >= 500 && r.statusCode <= 599) ||
      r.statusCode == 429 // Cloud Run LB returns this when there are no instances to handle the request

  val idempotentRetries: RetryFunction = {
    case (req, Return(res: Response)) if isRetryableStatus(res) =>
      logger.warn(s"Retrying response status 5xx on ${req.method} ${req.path}")
      true
    case (req, Throw(RetryableWriteException(_))) =>
      logger.warn(s"Retrying RetryableWriteException on ${req.method} ${req.path}")
      true
    case (req, Throw(e: TimeoutException)) =>
      logger.warn(s"Retrying $e on ${req.method} ${req.path}")
      true
    case (req, Throw(Failure(Some(e: TimeoutException)))) =>
      logger.warn(s"Retrying $e on ${req.method} ${req.path}")
      true
    case (req, Throw(e: UtilTimeoutException)) =>
      logger.warn(s"Retrying $e on ${req.method} ${req.path}")
      true
    case (req, Throw(Failure(Some(e: UtilTimeoutException)))) =>
      logger.warn(s"Retrying $e on ${req.method} ${req.path}")
      true
    case (req, Throw(e: ChannelClosedException)) =>
      logger.warn(s"Retrying $e on ${req.method} ${req.path}")
      true
  }
}
