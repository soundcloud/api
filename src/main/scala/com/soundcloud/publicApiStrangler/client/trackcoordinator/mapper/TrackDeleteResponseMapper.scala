package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.outcome._
import com.twitter.finagle.http.{Response, Status}

object TrackDeleteResponseMapper {
  def apply(response: Response): Outcome[Unit] = {
    response.status match {
      case Status.Accepted => Good(())
      case Status.NotFound => NotFound().bad
      case _ => NotValid(response.contentString).bad
    }
  }
}
