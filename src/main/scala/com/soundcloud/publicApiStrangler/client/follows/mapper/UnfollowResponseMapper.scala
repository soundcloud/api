package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow._
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.{JsValue, Json}

object UnfollowResponseMapper {

  def apply(response: Response): UnfollowResponse =
    response.status match {
      case Status.Ok => UnfollowSuccessful
      case Status.NotFound => UserNotFound
      case Status.UnprocessableEntity =>
        val data = Json.parse(response.contentString)
        (data \ "error" \ "name").asOpt[String] match {
          case Some("UserAsTarget") => UserAsTarget
          case Some("NotFollowing") => NotFollowing
          case _ => unknownError(Status.UnprocessableEntity, data)
        }
      case status => unknownError(status, Json.parse(response.contentString))
    }

  private def unknownError(status: Status, data: JsValue) =
    UnknownError(s"Unknown error: $data", status.code)
}
