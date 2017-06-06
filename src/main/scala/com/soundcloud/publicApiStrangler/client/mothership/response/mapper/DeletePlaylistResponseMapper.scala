package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.client.support.{ResponseMapper, UnhandledResponseException}
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
