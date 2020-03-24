package com.soundcloud.publicApiStrangler.client.tracks
import play.api.libs.json.{Json, Writes}

case class TracksParams(trackRequests: List[TrackRequest])

object TracksParams {
  implicit val writes: Writes[TracksParams] = Json.writes[TracksParams]
}
