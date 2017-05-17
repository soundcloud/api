package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.{OkResetUserPasswordResponse, ResetUserPasswordResponse, UserDoesntExistButDontExposeThisResetUserPasswordResponse}
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
