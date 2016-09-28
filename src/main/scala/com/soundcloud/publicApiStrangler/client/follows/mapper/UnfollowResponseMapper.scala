package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.publicApiStrangler.client.follows.representation.unfollow._
import com.soundcloud.scalakit.finagle.http.{NotFoundStatus, OkStatus, StatusCode, UnprocessableEntityStatus}
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import play.api.libs.json.JsValue

object UnfollowResponseMapper {

  def apply(response: JsonResponse): UnfollowResponse =
    response match {
      case JsonResponse(OkStatus, _, _, _) => UnfollowSuccessful
      case JsonResponse(NotFoundStatus, _, _, _) => UserNotFound
      case JsonResponse(UnprocessableEntityStatus, data, _, _) =>
        (data \ "error" \ "name").asOpt[String] match {
          case Some("UserAsTarget") => UserAsTarget
          case Some("NotFollowing") => NotFollowing
          case _ => unknownError(UnprocessableEntityStatus, data)
        }
      case JsonResponse(status, data, _, _) => unknownError(status, data)
    }

  private def unknownError(status: StatusCode, data: JsValue) =
    UnknownError(s"Unknown error: $data", status.i)
}
