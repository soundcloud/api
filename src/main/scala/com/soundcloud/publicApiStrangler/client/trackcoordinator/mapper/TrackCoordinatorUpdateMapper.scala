package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object TrackCoordinatorUpdateMapper {
  def apply(response: Response): Outcome[TrackCoordinatorTrack] = {
    response.status match {
      case Status.Ok => Json.parse(response.contentString).as[TrackCoordinatorTrack].good
      case Status.NotFound | Status.Unauthorized => NotFound().bad
      case Status.BadRequest => {
        NotValid(TrackCoordinatorError.extractTrackCoordinatorErrorMessage(response.contentString)).bad
      }
      case _ => throw UnhandledResponseException(response)
    }
  }
}
