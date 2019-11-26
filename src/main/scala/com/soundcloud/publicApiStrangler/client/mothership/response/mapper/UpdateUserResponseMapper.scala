package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  InvalidUrnUpdateUserResponse,
  NotAuthorizedUpdateUserResponse,
  OkUpdateUserResponse,
  UpdateUserResponse
}
import com.soundcloud.publicApiStrangler.client.support.{ResponseMapper, UnhandledResponseException}
import com.twitter.finagle.http.{Response, Status}

class UpdateUserResponseMapper extends ResponseMapper[UpdateUserResponse] {
  def apply(response: Response): UpdateUserResponse = {
    response match {
      case Status.Ok => OkUpdateUserResponse
      case Status.Unauthorized => NotAuthorizedUpdateUserResponse
      case Status.UnprocessableEntity => InvalidUrnUpdateUserResponse
      case r => throw new UnhandledResponseException(r)
    }
  }
}
