package com.soundcloud.apipublic.client.mothership.response.mapper

import com.soundcloud.apipublic.client.support.UnhandledResponseException
import com.soundcloud.jvmkit.module.outcome._
import com.twitter.finagle.http.{Response, Status}

class UpdateUserArtworkResponseMapper {
  def apply(response: Response): Outcome[Unit] = response.status match {
    case Status.Created => Good(())
    case Status.Unauthorized => Bad(NotAuthorized())
    case Status.Forbidden => Bad(NotAllowed())
    case Status.NotFound => Bad(NotFound("User not found"))
    case Status.BadRequest => Bad(NotValid("Bad request"))
    case _ => throw UnhandledResponseException(response)
  }
}
