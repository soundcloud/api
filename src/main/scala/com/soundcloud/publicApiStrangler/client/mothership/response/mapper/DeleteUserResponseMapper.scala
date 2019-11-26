package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  DeleteUserResponse,
  InvalidParametersDeleteUserResponse,
  OkDeleteUserResponse,
  UserNotFoundDeleteUserResponse
}
import com.soundcloud.publicApiStrangler.client.support.{ResponseMapper, UnhandledResponseException}
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
