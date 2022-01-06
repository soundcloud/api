package com.soundcloud.apipublic.authorization

import com.soundcloud.apipublic.authorization.policies.Reason
import com.soundcloud.apipublic.support.ErrorResponse
import com.twitter.finagle.http.{Response, Status}

object Reasonator {
  val NOT_FOUND = ErrorResponse(Status.NotFound, "This resource was removed or is unavailable.")
  val GEO_ERROR = ErrorResponse(Status.Forbidden, "Sorry, this track is not available in your area.")
  val CLIENT_APPLICATION_ERROR = ErrorResponse(Status.Forbidden, "This content is only available on SoundCloud.")
  val NOT_AVAILABLE_PLAYLIST_ERROR = ErrorResponse(Status.Forbidden, "This playlist is not available.")
  val NOT_SUPPORTED_ERROR =
    ErrorResponse(Status.Forbidden, "Sorry, this track is not compatible on your current device/platform.")

  def reasonToError(reason: Reason): Response = reason match {
    case Reason.GEO => GEO_ERROR
    case Reason.RIGHTSHOLDER_RESTRICTED => NOT_FOUND
    case Reason.CLIENT_APPLICATION => CLIENT_APPLICATION_ERROR
    case Reason.NOT_SUPPORTED => NOT_SUPPORTED_ERROR
    case _ => NOT_FOUND
  }
}
