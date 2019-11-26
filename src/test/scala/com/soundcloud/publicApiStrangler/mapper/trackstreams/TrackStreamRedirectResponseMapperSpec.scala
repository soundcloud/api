package com.soundcloud.publicApiStrangler.mapper.trackstreams

import com.soundcloud.jvmkit.module.util.Url
import com.soundcloud.publicApiStrangler.client.media.MediaUrl
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
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

  trait ExpectedUrlProvidedContext extends Context {
    val urls = Future.value(Set(httpMediaUrl))
    lazy val response = Await.result(mapper.map(urls, false))
    lazy val headResponse = Await.result(mapper.map(urls, true))
  }

  trait SeveralUrlsProvidedContext extends Context {
    val urls = Future.value(Set(hlsMediaUrl, previewMediaUrl, httpMediaUrl))
    lazy val response = Await.result(mapper.map(urls, false))
    lazy val headResponse = Await.result(mapper.map(urls, true))
  }

  trait ExpectedUrlNotProvidedContext extends Context {
    val urls = Future.value(Set(hlsMediaUrl, previewMediaUrl))
    lazy val response = Await.result(mapper.map(urls, false))
    lazy val headResponse = Await.result(mapper.map(urls, true))
  }

  "TrackStreamRedirectResponseMapper" should {
    "when expected URL is provided" >> {
      "and it is a GET request" >> {
        "return a 302" in new ExpectedUrlProvidedContext {
          response.status ==== Status.Found
        }

        "return a Location header" in new ExpectedUrlProvidedContext {
          response.headerMap.get("Location") ==== Some("http://regular")
        }

        "return a JSON response body" in new ExpectedUrlProvidedContext {
          response.contentString mustEqual """{"status":"302 - Found","location":"http://regular"}"""
        }
      }

      "and it is a HEAD request" >> {
        "return a 302" in new ExpectedUrlProvidedContext {
          headResponse.status ==== Status.Found
        }

        "return a Location header" in new ExpectedUrlProvidedContext {
          headResponse.headerMap.get("Location") ==== Some("http://regular")
        }

        "return no response body" in new ExpectedUrlProvidedContext {
          headResponse.contentString mustEqual ""
        }
      }
    }

    "when several URLs are provided" >> {
      "and it is a GET request" >> {
        "return a 302" in new SeveralUrlsProvidedContext {
          response.status ==== Status.Found
        }

        "return a Location header" in new SeveralUrlsProvidedContext {
          response.headerMap.get("Location") ==== Some("http://regular")
        }

        "return a JSON response body" in new SeveralUrlsProvidedContext {
          response.getContentString() mustEqual """{"status":"302 - Found","location":"http://regular"}"""
        }
      }

      "and it is a HEAD request" >> {
        "return a 302" in new SeveralUrlsProvidedContext {
          headResponse.status ==== Status.Found
        }

        "return a Location header" in new SeveralUrlsProvidedContext {
          headResponse.headerMap.get("Location") ==== Some("http://regular")
        }

        "return no response body" in new SeveralUrlsProvidedContext {
          headResponse.getContentString() mustEqual ""
        }
      }
    }

    "when expected URL is not provided" >> {
      "and it is a GET request" >> {
        "return a 404" in new ExpectedUrlNotProvidedContext {
          response.status ==== Status.NotFound
        }

        "return no content" in new ExpectedUrlNotProvidedContext {
          response.getContentString() mustEqual ""
        }
      }

      "and it is a HEAD request" >> {
        "return a 404" in new ExpectedUrlNotProvidedContext {
          headResponse.status ==== Status.NotFound
        }

        "return no content" in new ExpectedUrlNotProvidedContext {
          headResponse.getContentString() mustEqual ""
        }
      }
    }
  }
}
