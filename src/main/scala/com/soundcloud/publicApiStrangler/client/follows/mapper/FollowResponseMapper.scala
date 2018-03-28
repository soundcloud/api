package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.publicApiStrangler.client.follows.representation.Following
import com.soundcloud.publicApiStrangler.client.follows.representation.follow._
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json._

object FollowResponseMapper {

  def apply(response: Response): FollowResponse =
    response.status match {
      case Status.Created => FollowingCreated(Json.parse(response.contentString).as[Following])
      case Status.Ok => AlreadyFollowing
      case Status.NotFound => UserNotFound
      case Status.TooManyRequests => SpamBlocked
      case Status.Forbidden => BlockedByTarget
      case Status.UnprocessableEntity =>
        val data = Json.parse(response.contentString)
        (data \ "error" \ "name").asOpt[String] match {
          case Some("MaxFollowingsReached") => MaxFollowingsReached
          case Some("UserAsTarget") => UserAsTarget
          case Some("AgeRestrictedUser") => AgeRestrictedUser
          case Some("AgeUnknownUser") => AgeUnknownUser
          case _ => unknownError(Status.UnprocessableEntity, data)
        }
      case status => unknownError(status, Json.parse(response.contentString))
    }

  private def unknownError(status: Status, data: JsValue) =
    UnknownError(s"Unknown error: $data", status.code)
}
