package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation._
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object TrackUpdateResponseMapper {
  def apply(response: Response): Result[Track] = {
    response.status match {
      case Status.Ok => Success(TrackMapper(Json.parse(response.contentString)))
      case Status.UnprocessableEntity => UnprocessableEntity(Result.parseErrors(Json.parse(response.contentString)))
      case Status.NotFound => NotFound(Nil)
      case other => throw new IllegalArgumentException(s"Unexpected track update response $other")
    }
  }
}
