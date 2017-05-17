package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation._
import com.twitter.finagle.http.{Response, Status}


class DeletePlaylistResponseMapper extends ResponseMapper[DeletePlaylistResponse] {
  def apply(response: Response) = {
    response.status match {
      case Status.Ok | Status.Accepted => OkDeletePlaylistResponse
      case Status.Unauthorized => NotAuthorizedDeletePlaylistResponse
      case Status.Forbidden => ForbiddenDeletePlaylistResponse
      case Status.NotFound => InvalidUrnDeletePlaylistResponse
      case _ => throw new UnhandledResponseException(response)
    }
  }
}
