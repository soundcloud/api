package com.soundcloud.bff.nextbff.repository

import com.soundcloud.scalakit.finagle.http.StatusCode
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse

case class RepositoryException(status: StatusCode, message: String) extends Exception(message)

object RepositoryException {
  def apply(jsonResponse: JsonResponse) =
    new RepositoryException(
      jsonResponse.status, s"invalid response received: [status=${jsonResponse.status.s}, json=${jsonResponse.body}]"
    )
}
