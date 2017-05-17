package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.representation.{DeleteFromPlaylistResponse, InvalidUrnDeleteFromPlaylistResponse, NotAuthorizedDeleteFromPlaylistResponse, OkDeleteFromPlaylistResponse}
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
