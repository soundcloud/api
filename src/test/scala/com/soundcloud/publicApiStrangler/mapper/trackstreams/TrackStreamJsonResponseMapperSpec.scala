package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.jvmkit.module.util.Url
import com.soundcloud.publicApiStrangler.client.media.MediaUrl
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
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
      }

      "and it is a GET request" >> {
        "return a 200" in new AllUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, false))
          response.status ==== Status.Ok
        }

        "return all URLs as JSON" in new AllUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, false))
          response.getContentString() mustEqual """{"http_mp3_128_url":"http://regular","rtmp_mp3_128_url":"http://rtmp","hls_mp3_128_url":"http://hls","preview_mp3_128_url":"http://preview"}"""
        }
      }

      "and it is a HEAD request" >> {
        "return a 200" in new AllUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, true))
          response.status ==== Status.Ok
        }

        "return no body" in new AllUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, true))
          response.getContentString() mustEqual ""
        }
      }
    }

    "when no URLs are available" >> {

      trait NoUrlsAvailableContext extends Context {
        val urls = Future.value(Set[MediaUrl]())
      }

      "and it is a GET request" >> {
        "return a 404" in new NoUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, false))
          response.status ==== Status.NotFound
        }

        "return an empty body" in new NoUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, false))
          response.getContentString() mustEqual ""
        }
      }

      "and it is a HEAD request" >> {
        "return a 404" in new NoUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, true))
          response.status ==== Status.NotFound
        }

        "return no body" in new NoUrlsAvailableContext {
          val response = Await.result(mapper.map(urls, true))
          response.getContentString() mustEqual ""
        }
      }
    }

  }

}
