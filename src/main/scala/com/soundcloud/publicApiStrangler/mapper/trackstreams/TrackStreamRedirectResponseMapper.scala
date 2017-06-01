package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.publicApiStrangler.media.MediaUrl
import com.twitter.finagle.http.{MediaType, Response, Status}
import com.twitter.util.Future
import play.api.libs.json.Json

/**
  * Maps a set of stream media urls to a redirect response.
  */
class TrackStreamRedirectResponseMapper extends TrackStreamResponseMapper {

  val urlOfInterest = "http_mp3_128_url"

  def map(mediaUrls: Future[Set[MediaUrl]], isHeadRequest: Boolean): Future[Response] = {
    val filteredUrls = mediaUrls.map(set => set.filter(url => url.name.equals(urlOfInterest)))
    filteredUrls.map { set =>
      set.headOption match {
        case None => ResponseBuilder.notFound()
        case Some(url) => mapResponse(url, isHeadRequest)
      }
    }
  }

  private def mapResponse(url: MediaUrl, isHeadRequest: Boolean): Response = {
    val builder = ResponseBuilder()
      .header("Location", url.url.s)
      .status(Status.Found)

    if (!isHeadRequest) {
      val content = Json.obj("status" -> "302 - Found", "location" -> url.url.s)
      builder.mediaType(MediaType.Json).body(Json.stringify(content)).build
    } else {
      builder.build
    }
  }
}
