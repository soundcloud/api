package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.client.JsonResponse
import com.soundcloud.publicApiStrangler.representation.{InvalidUrnUpdatePlaylistResponse, NotAuthorizedUpdatePlaylistResponse, OkUpdatePlaylistResponse, UpdatePlaylistResponse}
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.JsObject

class UpdatePlaylistResponseMapper extends ResponseMapper[UpdatePlaylistResponse] {
  override def apply(response: Response) =
    JsonResponse.from(response) match {
      case JsonResponse(Status.Ok, Right(json: JsObject), _) => OkUpdatePlaylistResponse(PlaylistMapper(json))
      case JsonResponse(Status.Unauthorized, _, _) => NotAuthorizedUpdatePlaylistResponse
      case JsonResponse(Status.NotFound, _, _) => InvalidUrnUpdatePlaylistResponse
      case _ => throw new UnhandledResponseException(response)
    }
}
