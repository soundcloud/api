package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.jvmkit.module.http.server.{JsonResponseBuilder, ResponseBuilder}
import com.soundcloud.publicApiStrangler.client.media.MediaUrl
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Maps track stream media URLs to our expected JSON response.
  */
class TrackStreamJsonResponseMapper extends TrackStreamResponseMapper {
  def map(mediaUrls: Future[Set[MediaUrl]], isHeadRequest: Boolean): Future[Response] = {
    mediaUrls.map { urls =>
      if (urls.isEmpty) {
        ResponseBuilder.notFound()
      } else {
        mapResponse(urls, isHeadRequest)
      }
    }
  }

  private def mapResponse(mediaUrls: Set[MediaUrl], isHeadRequest: Boolean): Response = {
    if (!isHeadRequest) {
      val jsonObjects = mediaUrls.map(url => Json.obj(url.name -> url.url.s))
      val json = jsonObjects.reduceLeft(_ ++ _)
      JsonResponseBuilder.ok(Json.stringify(json))
    } else {
      ResponseBuilder.ok()
    }
  }
}
