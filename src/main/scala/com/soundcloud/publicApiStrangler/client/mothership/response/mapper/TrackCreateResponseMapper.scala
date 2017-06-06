package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Result, Success, Track, UnprocessableEntity}
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object TrackCreateResponseMapper {
  def apply(response: Response): Result[Track] = {
    response.status match {
      case Status.Created => Success(TrackMapper(Json.parse(response.contentString)), Status.Created)
      case Status.UnprocessableEntity => UnprocessableEntity(Result.parseErrors(Json.parse(response.contentString)))
      case other => throw new IllegalArgumentException(s"Unexpected track create response $other")
    }
  }
}
