package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.tracks.TrackMetadataUpdateResult
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object TrackMetadataUpdateMapper {
  def apply(response: Response): Outcome[TrackMetadataUpdateResult] = {
    response.status match {
      case Status.Ok => Json.parse(response.contentString).as[TrackMetadataUpdateResult].good
      case Status.NotFound => NotFound().bad
      case _ => NotValid("invalid request").bad
    }
  }
}
