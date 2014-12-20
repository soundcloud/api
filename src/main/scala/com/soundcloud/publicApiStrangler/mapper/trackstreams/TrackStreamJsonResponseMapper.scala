package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.media.MediaUrl
import com.twitter.util.Future
import play.api.libs.json.Json

/**
 * Maps track stream Media Urls to our expected json response.
 *
 */
class TrackStreamJsonResponseMapper extends TrackStreamResponseMapper {


  val urlNamesOfInterest = Set("http_mp3_128_url", "preview_mp3_128_url")

  def map(mediaUrls: Future[Set[MediaUrl]]) : Future[ResponseBuilder] = {
    val urlsOfInterest = mediaUrls.map(set => set.filter(url => urlNamesOfInterest.contains(url.name)))
    urlsOfInterest.map(set =>
      if (set.isEmpty)
        new ResponseBuilder().notFound
      else
        mapResponse(set)
    )
  }

  private def mapResponse(mediaUrls:Set[MediaUrl]) : ResponseBuilder = {
    val jsonObjects = mediaUrls.map(url => Json.obj( url.name -> url.url.s ))
    val json = jsonObjects.reduceLeft(_ ++ _)
    new ResponseBuilder().typedJson(json)
  }

}
