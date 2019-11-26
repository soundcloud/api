package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  DeleteFromPlaylistResponse,
  InvalidUrnDeleteFromPlaylistResponse,
  NotAuthorizedDeleteFromPlaylistResponse,
  OkDeleteFromPlaylistResponse
}
import com.soundcloud.publicApiStrangler.client.support.{ResponseMapper, UnhandledResponseException}
import com.twitter.finagle.http.{Response, Status}

class DeleteFromPlaylistResponseMapper extends ResponseMapper[DeleteFromPlaylistResponse] {
  def apply(response: Response) = {
    response.status match {
      case Status.Ok => OkDeleteFromPlaylistResponse
      case Status.Unauthorized => NotAuthorizedDeleteFromPlaylistResponse
      case Status.NotFound => InvalidUrnDeleteFromPlaylistResponse
      case _ => throw new UnhandledResponseException(response)
    }
  }
}
