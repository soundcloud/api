package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object TrackCoordinatorTrackMapper {
  def apply(response: Response): Outcome[TrackCoordinatorTrack] = {

    response.status match {
      case Status.Ok => Json.parse(response.contentString).as[TrackCoordinatorTrack].good
      case Status.NotFound => NotFound().bad
      case _ => throw UnhandledResponseException(response)
    }
  }
}
