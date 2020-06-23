package com.soundcloud.publicApiStrangler.client.liebling

import com.twitter.finagle.http.{Response, Status}

object DeleteLikeResponseMapper {
  def apply(response: Response): DeleteLikeResponse =
    response.status match {
      case Status.Ok => LikeDeleted
      case _ => LikeNotFound
    }
}
