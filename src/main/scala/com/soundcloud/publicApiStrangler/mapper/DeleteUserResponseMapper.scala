package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.{DeleteUserResponse, InvalidParametersDeleteUserResponse, OkDeleteUserResponse, UserNotFoundDeleteUserResponse}
import com.twitter.finagle.http.{Response, Status}

class DeleteUserResponseMapper extends ResponseMapper[DeleteUserResponse] {
  override def apply(response: Response): DeleteUserResponse = {
    response.status match {
      case Status.Ok => OkDeleteUserResponse
      case Status.BadRequest => InvalidParametersDeleteUserResponse
      case Status.UnprocessableEntity => InvalidParametersDeleteUserResponse
      case Status.NotFound => UserNotFoundDeleteUserResponse
      case Status.Conflict | _ => throw new UnhandledResponseException(response)
    }
  }
}
