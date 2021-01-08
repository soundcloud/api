package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import play.api.libs.json.{Json, Reads}

case class TrackCoordinatorErrorResponse(errors: Seq[TrackCoordinatorError])
case class TrackCoordinatorError(message: String, error_type: Option[String])

object TrackCoordinatorErrorResponse {
  implicit val reads: Reads[TrackCoordinatorErrorResponse] = Json.reads[TrackCoordinatorErrorResponse]
}

object TrackCoordinatorError {
  implicit val reads: Reads[TrackCoordinatorError] = Json.reads[TrackCoordinatorError]
}
