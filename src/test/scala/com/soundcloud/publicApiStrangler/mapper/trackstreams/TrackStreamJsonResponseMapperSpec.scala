package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.bff.media.MediaUrl
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Url
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime


class TrackStreamJsonResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val expiresAt = DateTime.now
    val previewMediaUrl = new MediaUrl("preview_mp3_128_url", Url("http://preview"), expiresAt)
    val httpMediaUrl = new MediaUrl("http_mp3_128_url", Url("http://regular"), expiresAt)
    val hlsMediaUrl = new MediaUrl("hls_mp3_128_url", Url("http://hls"), expiresAt)
    val rtmpMediaUrl = new MediaUrl("rtmp_mp3_128_url", Url("http://rtmp"), expiresAt)
    val mapper = new TrackStreamJsonResponseMapper
  }

  "TrackStreamJsonResponseMapper" should {

    "Build json response with status code 200 and all urls when urls are available" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set(httpMediaUrl, rtmpMediaUrl, hlsMediaUrl, previewMediaUrl))))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 200
      response.getContentString() mustEqual """{"http_mp3_128_url":"http://regular","rtmp_mp3_128_url":"http://rtmp","hls_mp3_128_url":"http://hls","preview_mp3_128_url":"http://preview"}"""
    }

    "Return Not Found response in case no urls are returned" in new Context {
      val responseBuilder = Await.result(mapper.map(Future.value(Set())))
      val response = responseBuilder.build

      response.getStatusCode() mustEqual 404
      response.getContentString() mustEqual ""
    }
  }

}
