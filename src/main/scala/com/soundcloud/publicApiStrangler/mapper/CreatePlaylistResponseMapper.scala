package com.soundcloud.publicApiStrangler.mapper

import com.soundcloud.publicApiStrangler.client.JsonResponse
import com.soundcloud.publicApiStrangler.representation.Playlist
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.Successful
import play.api.libs.json.JsObject

class CreatePlaylistResponseMapper extends ResponseMapper[Playlist] {
  override def apply(response: Response) = {
    JsonResponse.from(response) match {
      case JsonResponse(Successful(_), Right(json: JsObject), _) => PlaylistMapper(json)
      case _ => throw new UnhandledResponseException(response)
    }
  }
}
