package com.soundcloud.publicApiStrangler.client.follows.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.JsonResponse
import com.soundcloud.publicApiStrangler.client.JsonResponse.stringify
import com.soundcloud.publicApiStrangler.client.follows.representation.Following
import com.soundcloud.publicApiStrangler.client.follows.representation.follow.{BulkFollowFailed, FollowResponse, FollowingCreated, UnknownError}
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.JsObject

object BulkFollowResponseMapper {

  def apply(response: Response, targets: List[Urn]): List[FollowResponse] =
    JsonResponse.from(response) match {
      case JsonResponse(Status.Created, Right(json: JsObject), _) => json.as[List[Following]].map(FollowingCreated.apply)
      case JsonResponse(Status.BadRequest, _, _) => List(BulkFollowFailed(targets))
      case JsonResponse(status, body, _) => List(UnknownError(stringify(body), status.code))
    }
}
