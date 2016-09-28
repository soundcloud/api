package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.publicApiStrangler.client.trackcoordinator.ErrorParser
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes._
import com.soundcloud.scalakit.finagle.http.{AcceptedStatus, NotFoundStatus}
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse

object TrackDeleteResponseMapper {
  def apply(response: JsonResponse): Result[Unit] = {
    response.status match {
      case AcceptedStatus => Success(())
      case NotFoundStatus => NotFound
      case _ => ServerError(ErrorParser.parse(response.body))
    }
  }
}
