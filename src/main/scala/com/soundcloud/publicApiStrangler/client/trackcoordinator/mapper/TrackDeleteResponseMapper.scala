package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.publicApiStrangler.client.trackcoordinator.ErrorParser
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes._
import com.twitter.finagle.http.{Response, Status}

object TrackDeleteResponseMapper {
  def apply(response: Response): Result[Unit] = {
    response.status match {
      case Status.Accepted => Success(())
      case Status.NotFound => NotFound
      case _ => ServerError(ErrorParser.parse(response.contentString))
    }
  }
}
