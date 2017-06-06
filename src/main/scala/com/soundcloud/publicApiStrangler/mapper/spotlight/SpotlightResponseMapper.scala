package com.soundcloud.publicApiStrangler.mapper.spotlight

import com.soundcloud.publicApiStrangler.client.support.{JsonResponse, ResponseMapper, UnhandledResponseException}
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.Status.Successful
import play.api.libs.json.JsArray

class SpotlightResponseMapper extends ResponseMapper[Spotlight] {
  override def apply(response: Response): Spotlight =
    JsonResponse.from(response) match {
      case JsonResponse(Successful(_), Right(json: JsArray), _) => SpotlightMapper(json)
      case _ => throw new UnhandledResponseException(response)
    }
}
