package com.soundcloud.apipublic.client.trackcoordinator

import com.soundcloud.jvmkit.module.outcome.{GoodOps, HttpResponseFields, HttpServiceError, NotFound, NotValid, Outcome}
import com.soundcloud.apipublic.client.trackcoordinator.mapper.TrackCoordinatorError
import com.soundcloud.apipublic.service.users.UserUploadQuota
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.Json

object UserUploadQuotaMapper {
  def apply(response: Response): Outcome[UserUploadQuota] = {
    response.status match {
      case Status.Ok => (Json.parse(response.contentString) \ "upload_duration").as[UserUploadQuota].good
      case Status.NotFound | Status.Unauthorized => NotFound().bad
      case Status.BadRequest =>
        NotValid(TrackCoordinatorError.extractTrackCoordinatorErrorMessage(response.contentString)).bad
      case _ => HttpServiceError(HttpResponseFields(response.statusCode)).bad
    }
  }
}
