package com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Try
import play.api.libs.json.Json

object TrackCoordinatorCreateMapper {
  def apply(response: Response): Outcome[TrackCoordinatorTrack] = {

    response.status match {
      case Status.Created => Json.parse(response.contentString).as[TrackCoordinatorTrack].good
      case Status.BadRequest => NotValid(extractTrackCoordinatorErrorMessage(response.contentString)).bad
      case Status.NotFound => NotFound().bad
      case _ => throw UnhandledResponseException(response)
    }
  }

  private def extractTrackCoordinatorErrorMessage(responseBody: String): String = {
    val errorMessage = for {
      json <- Try(Json.parse(responseBody)).toOption
      response <- json.validate[TrackCoordinatorErrorResponse].asOpt
    } yield response.errors.head.message
    errorMessage.getOrElse("invalid request")
  }

}
