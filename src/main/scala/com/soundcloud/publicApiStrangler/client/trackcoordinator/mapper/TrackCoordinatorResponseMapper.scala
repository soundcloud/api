package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.twitter.finagle.http.{Response, Status}

object TrackCoordinatorResponseMapper {
  def apply(response: Response): Outcome[Unit] = {

    response.status match {
      case Status.Accepted | Status.Ok => Good(())
      case Status.NotFound => NotFound().bad
      case _ => throw UnhandledResponseException(response)
    }
  }
}
