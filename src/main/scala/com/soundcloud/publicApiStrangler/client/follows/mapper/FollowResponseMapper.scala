package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.publicApiStrangler.client.follows.representation.Following
import com.soundcloud.publicApiStrangler.client.follows.representation.follow._
import com.soundcloud.scalakit.finagle.http._
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import play.api.libs.json._

object FollowResponseMapper {

  def apply(response: JsonResponse): FollowResponse =
    response match {
      case JsonResponse(CreatedStatus, data: JsObject, _, _) => FollowingCreated(data.as[Following])
      case JsonResponse(OkStatus, _, _, _) => AlreadyFollowing
      case JsonResponse(NotFoundStatus, _, _, _) => UserNotFound
      case JsonResponse(TooManyRequestsStatus, data: JsObject, _, _) => SpamBlocked((data \ "spam_warnings").as[List[JsObject]])
      case JsonResponse(ForbiddenStatus, _, _, _) => BlockedByTarget
      case JsonResponse(UnprocessableEntityStatus, data, _, _) =>
        (data \ "error" \ "name").asOpt[String] match {
          case Some("MaxFollowingsReached") => MaxFollowingsReached
          case Some("UserAsTarget") => UserAsTarget
          case Some("AgeRestrictedUser") => AgeRestrictedUser
          case Some("AgeUnknownUser") => AgeUnknownUser
          case _ => unknownError(UnprocessableEntityStatus, data)
        }
      case JsonResponse(status, data, _, _) => unknownError(status, data)
    }

  private def unknownError(status: StatusCode, data: JsValue) =
    UnknownError(s"Unknown error: $data", status.i)
}
