package com.soundcloud.publicApiStrangler.client.liebling

import com.twitter.finagle.http.{Response, Status}

object CreateLikeResponseMapper {
  def apply(response: Response): CreateLikeResponse =
    response.status match {
      case Status.Ok => LikeAlreadyExists
      case Status.Created => LikeCreated
      case Status.Forbidden => UserBlocked
      case Status.NotFound => LikeableNotFound
      case Status.UnprocessableEntity => UrnNotValid
      case Status.TooManyRequests => UserHasSpamWarning
      case Status(_) => LikeableNotFound
    }
}
