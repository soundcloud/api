package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.publicApiStrangler.client.media.MediaUrl
import com.twitter.finagle.http.Response
import com.twitter.util.Future

/**
  * Maps an eventual set of media urls to a response builder.
  */
trait TrackStreamResponseMapper {
  def map(mediaUrls: Future[Set[MediaUrl]], isHeadRequest: Boolean): Future[Response]
}
