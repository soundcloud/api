package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.follows.representation.Following
import com.soundcloud.publicApiStrangler.client.follows.representation.follow.{BulkFollowFailed, FollowResponse, FollowingCreated, UnknownError}
import com.soundcloud.scalakit.finagle.http.{BadRequestStatus, CreatedStatus}
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import play.api.libs.json.{JsObject, Json}

object BulkFollowResponseMapper {

  def apply(response: JsonResponse, targets: List[Urn]): List[FollowResponse] =
    response match {
      case JsonResponse(CreatedStatus, data: JsObject, _, _) => data.as[List[Following]].map(FollowingCreated.apply)
      case JsonResponse(BadRequestStatus, _, _, _) => List(BulkFollowFailed(targets))
      case JsonResponse(status, data, _, _) => List(UnknownError(Json.stringify(data), status.i))
    }
}
