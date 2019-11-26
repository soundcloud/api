package com.soundcloud.bff.nextbff.repository

import com.twitter.finagle.http.{Response, Status}

case class RepositoryException(status: Status, message: String) extends Exception(message)

object RepositoryException {
  def apply(response: Response) =
    new RepositoryException(
      response.status,
      s"invalid response received: [status=${response.statusCode}, body=${response.contentString}]"
    )
}
