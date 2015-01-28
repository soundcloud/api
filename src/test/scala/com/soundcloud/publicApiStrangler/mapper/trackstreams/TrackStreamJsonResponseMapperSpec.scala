package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.media.MediaUrl
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Url
import com.twitter.util.{Await, Future}


class TrackStreamJsonResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val previewMediaUrl = new MediaUrl("preview_mp3_128_url", Url("http://preview"))
    val httpMediaUrl = new MediaUrl("http_mp3_128_url", Url("http://regular"))
    val hlsMediaUrl = new MediaUrl("hls_mp3_128_url", Url("http://hls"))
    val mapper = new TrackStreamJsonResponseMapper
  }

  "TrackStreamJsonResponseMapper" should {

    "Build json response with status code 200 when expected urls are provided" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(httpMediaUrl, previewMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 200
      response.getContentString() mustEqual """{"http_mp3_128_url":"http://regular","preview_mp3_128_url":"http://preview"}"""
    }

    "Filter out urls not of our interest" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(httpMediaUrl, previewMediaUrl, hlsMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 200
      response.getContentString() mustEqual """{"http_mp3_128_url":"http://regular","preview_mp3_128_url":"http://preview"}"""
    }

    "Return Not Found response in case expected url is not returned" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(hlsMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 404
      response.getContentString() mustEqual ""
    }
  }

}
