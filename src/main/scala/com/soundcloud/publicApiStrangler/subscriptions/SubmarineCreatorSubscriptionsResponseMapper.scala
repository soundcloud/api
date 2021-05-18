package com.soundcloud.publicApiStrangler.subscriptions

import com.soundcloud.publicApiStrangler.client.support.ResponseMapper
import play.api.libs.json.Json
import com.twitter.finagle.http.{Response, Status}

class SubmarineCreatorSubscriptionsResponseMapper(
    creatorSubscriptionsMapper: SubmarineCreatorSubscriptionsMapper = new SubmarineCreatorSubscriptionsMapper()
) extends ResponseMapper[SubmarineCreatorSubscriptionsResponse] {
  override def apply(response: Response): SubmarineCreatorSubscriptionsResponse = {

    response.status match {
      case Status.Ok =>
        ActiveSubmarineCreatorSubscriptionsResponse(creatorSubscriptionsMapper(Json.parse(response.contentString)))
      case _ => NoSubmarineCreatorSubscriptionsResponse
    }
  }
}
