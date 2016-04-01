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

    "when URLs are available" >> {
      trait AllUrlsAvailableContext extends Context {
        val urls = Future.value(Set(httpMediaUrl, rtmpMediaUrl, hlsMediaUrl, previewMediaUrl))
        val responseBuilder = Await.result(mapper.map(urls, false))
        val response = responseBuilder.build
      }

      "return a 200" in new AllUrlsAvailableContext {
        response.getStatusCode() mustEqual 200
      }

      "return all URLs as JSON" in new AllUrlsAvailableContext {
        response.getContentString() mustEqual """{"http_mp3_128_url":"http://regular","rtmp_mp3_128_url":"http://rtmp","hls_mp3_128_url":"http://hls","preview_mp3_128_url":"http://preview"}"""
      }
    }

    "when no URLs are available" >> {

      trait NoUrlsAvailableContext extends Context {
        val urls = Future.value(Set[MediaUrl]())
        val responseBuilder = Await.result(mapper.map(urls, false))
        val response = responseBuilder.build
      }

      "return a 404" in new NoUrlsAvailableContext {
        response.getStatusCode() mustEqual 404
      }

      "return an empty body" in new NoUrlsAvailableContext {
        response.getContentString() mustEqual ""
      }
    }

    // TODO: HEAD requests
  }

}
