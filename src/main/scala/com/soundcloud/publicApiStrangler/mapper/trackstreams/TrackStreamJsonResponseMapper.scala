package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.media.MediaUrl
import com.twitter.util.Future
import play.api.libs.json.Json

/**
 * Maps track stream media URLs to our expected JSON response.
 */
class TrackStreamJsonResponseMapper extends TrackStreamResponseMapper {

  def map(mediaUrls: Future[Set[MediaUrl]], isHeadRequest: Boolean): Future[ResponseBuilder] = {
    mediaUrls.map { urls =>
      if (urls.isEmpty) {
        new ResponseBuilder().notFound
      } else {
        mapResponse(urls, isHeadRequest)
      }
    }
  }

  private def mapResponse(mediaUrls: Set[MediaUrl], isHeadRequest: Boolean): ResponseBuilder = {
    val jsonObjects = mediaUrls.map(url => Json.obj(url.name -> url.url.s))
    val json = jsonObjects.reduceLeft(_ ++ _)
    new ResponseBuilder().typedJson(json)
  }
}
