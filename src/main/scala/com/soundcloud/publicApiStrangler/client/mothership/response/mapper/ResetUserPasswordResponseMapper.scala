package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  OkResetUserPasswordResponse,
  ResetUserPasswordResponse,
  UserDoesntExistButDontExposeThisResetUserPasswordResponse
}
import com.soundcloud.publicApiStrangler.client.support.{ResponseMapper, UnhandledResponseException}
import com.twitter.finagle.http.Response

class ResetUserPasswordResponseMapper extends ResponseMapper[ResetUserPasswordResponse] {
  override def apply(response: Response) = {
    response.statusCode match {
      case 205 => OkResetUserPasswordResponse
      case 404 => UserDoesntExistButDontExposeThisResetUserPasswordResponse
      case code => throw new UnhandledResponseException(response)
    }
  }
}
