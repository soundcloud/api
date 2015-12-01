package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.media.MediaUrl
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Url
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime


class TrackStreamRedirectResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val expiresAt = DateTime.now
    val previewMediaUrl = new MediaUrl("preview_mp3_128_url", Url("http://preview"), expiresAt)
    val httpMediaUrl = new MediaUrl("http_mp3_128_url", Url("http://regular"), expiresAt)
    val hlsMediaUrl = new MediaUrl("hls_mp3_128_url", Url("http://hls"), expiresAt)
    val mapper = new TrackStreamRedirectResponseMapper
  }

  "TrackStreamRedirectResponseMapper" should {

    "Return redirect response in case expected url is provided" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(httpMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 302
      response.getContentString() mustEqual "{\"status\":\"302 - Found\",\"location\":\"http://regular\"}"
      response.headers().get("Location") mustEqual "http://regular"
    }

    "Filter out not needed urls in case multiple are provided" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(hlsMediaUrl, previewMediaUrl, httpMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 302
      response.getContentString() mustEqual "{\"status\":\"302 - Found\",\"location\":\"http://regular\"}"
      response.headers().get("Location") mustEqual "http://regular"
    }

    "Return not found response in case expected url is not returned" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(hlsMediaUrl, previewMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 404
      response.getContentString() mustEqual ""
    }

  }


}
