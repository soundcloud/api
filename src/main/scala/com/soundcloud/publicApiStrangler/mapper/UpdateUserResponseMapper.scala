package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.{InvalidUrnUpdateUserResponse, NotAuthorizedUpdateUserResponse, OkUpdateUserResponse, UpdateUserResponse}
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
