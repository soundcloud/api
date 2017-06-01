package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation._
import com.twitter.finagle.http.Response

class AddToPlaylistResponseMapper extends ResponseMapper[AddToPlaylistResponse] {
  override def apply(response: Response) = {
    response.statusCode match {
      case 200 => OkAddToPlaylistResponse
      case 401 => NotAuthorizedAddToPlaylistResponse
      case 404 => InvalidUrnAddToPlaylistResponse
      case 409 => TrackAlreadyInPlaylistAddToPlaylistResponse
      case _ => throw new UnhandledResponseException(response)
    }
  }
}
