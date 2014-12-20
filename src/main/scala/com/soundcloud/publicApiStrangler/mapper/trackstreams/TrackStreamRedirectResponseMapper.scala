package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.media.MediaUrl
import com.twitter.util.Future
import play.api.libs.json.Json

/**
 * Maps a set of stream media urls to a redirect response.
 */
class TrackStreamRedirectResponseMapper extends TrackStreamResponseMapper {

  val urlOfInterest = "http_mp3_128_url"

  def map(mediaUrls: Future[Set[MediaUrl]]) : Future[ResponseBuilder] = {
    val filteredUrls = mediaUrls.map(set => set.filter(url => url.name.equals(urlOfInterest)))
    filteredUrls.flatMap(set =>
      set.headOption match {
        case None => Future.value(new ResponseBuilder().notFound)
        case Some(url) => buildResponse(url)
      }
    )
  }

  private def buildResponse(url:MediaUrl) : Future[ResponseBuilder] = {
    val content = Json.obj("status" -> "302 - Found", "location" -> url.url.s)
    val rb = new ResponseBuilder
    rb.typedJson(content).header("Location", url.url.s).status(302)
    Future.value(rb)
  }

}
