package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future, Try}
import play.api.libs.json.{JsNull, Json}

class MediaServiceClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val userSession = mock[UserSession]
    val client = new MediaServiceClient(jsonClient)

    val trackUid = "RV7fg1Q3h1lF"
  }

  trait StreamContext extends Context {
    val transcodingUuid = "some-uuid"
    val protocol = "progressive"
    val url = "http://some-url"
  }

  "#fetchTranscodings" >> {
    "returns some transcodings" in new Context {
      jsonClient.getWithSession(userSession, Path() / "transcodings", Params("upload_ids" -> trackUid), Headers.empty) returns
        Future.value(jsonResponse(Status.Ok, Fixtures.withContentsOf("media-service", "transcodings")))

      Await.result(client.fetchTranscodings(userSession, trackUid)) ==== List(
        Transcoding("cfc36f60-226a-4c16-9a5c-533b306f05f9", "audio/mp4; codecs=\"mp4a.40.2\"")
      )
    }

    "returns no transcodings" in new Context {
      jsonClient.getWithSession(userSession, Path() / "transcodings", Params("upload_ids" -> trackUid), Headers.empty) returns
        Future.value(jsonResponse(Status.Ok, Json.obj("transcodings" -> Json.obj())))

      Await.result(client.fetchTranscodings(userSession, trackUid)) ==== List()
    }

    "returns a 500" in new Context {
      jsonClient.getWithSession(userSession, Path() / "transcodings", Params("upload_ids" -> trackUid), Headers.empty) returns
        Future.value(jsonResponse(Status.InternalServerError, JsNull))

      Await.result(client.fetchTranscodings(userSession, trackUid)) should throwAn[UnhandledResponseException]
    }
  }

  "#fetchStreamUrl" >> {
    "returns a stream url" in new StreamContext {
      jsonClient.getWithSession(userSession, Path() / "stream" / transcodingUuid / protocol, Params.empty, Headers.empty) returns
        Future.value(jsonResponse(Status.Ok, Json.obj("url" -> url)))

      Await.result(client.fetchStreamUrl(userSession, transcodingUuid, protocol)) ==== Some(url)
    }

    "returns a 500" in new StreamContext {
      jsonClient.getWithSession(userSession, Path() / "stream" / transcodingUuid / protocol, Params.empty, Headers.empty) returns
        Future.value(jsonResponse(Status.InternalServerError, JsNull))

      Try(Await.result(client.fetchStreamUrl(userSession, transcodingUuid, protocol))).throwable should beAnInstanceOf[UnhandledResponseException]
    }
  }

  "#fetchPreviewUrl" >> {
    "returns a stream url" in new StreamContext {
      jsonClient.getWithSession(userSession, Path() / "preview" / transcodingUuid / protocol, Params.empty, Headers.empty) returns
        Future.value(jsonResponse(Status.Ok, Json.obj("url" -> url)))

      Await.result(client.fetchPreviewUrl(userSession, transcodingUuid, protocol)) ==== Some(url)
    }

    "returns a 500" in new StreamContext {
      jsonClient.getWithSession(userSession, Path() / "preview" / transcodingUuid / protocol, Params.empty, Headers.empty) returns
        Future.value(jsonResponse(Status.InternalServerError, JsNull))

      Try(Await.result(client.fetchPreviewUrl(userSession, transcodingUuid, protocol))).throwable should beAnInstanceOf[UnhandledResponseException]
    }
  }

  "#fetchDownloadOriginalUrl" >> {
    trait DownloadContext extends Context {
      val response: Future[Response]
      jsonClient.getWithSession(userSession, Path() / "download-original" / trackUid, Params.empty, Headers.empty) returns response

      lazy val result = Await.result(client.fetchDownloadOriginalUrl(userSession, trackUid))
    }

    "when URL is available for download" >> {
      trait AvailableDownloadContext extends DownloadContext {
        override lazy val response = Future.value(jsonResponse(Status.Ok, Json.obj("url" -> "http://download-url")))
      }

      "URL is returned" in new AvailableDownloadContext {
        result ==== Some("http://download-url")
      }
    }
    
    "when URL is missing" >> {
      trait MissingDownloadContext extends DownloadContext {
        override lazy val response = Future.value(ResponseBuilder.notFound())
      }

      "Nothing is returned" in new MissingDownloadContext {
        result ==== None
      }
    }
    
    "when response is missing a URL" >> {
      trait MissingUrlContext extends DownloadContext {
        override lazy val response = Future.value(jsonResponse(Status.Ok, Json.obj()))
      }

      "Nothing is returned" in new MissingUrlContext {
        result ==== None
      }
    }
    
    "when response is not in JSON format" >> {
      trait MalformedResponseContext extends DownloadContext {
        override lazy val response = Future.value(ResponseBuilder.ok("not json"))
      }

      "An exception is thrown" in new MalformedResponseContext {
        result should throwAn[Exception]
      }
    }

    "when response status is not expected" >> {
      trait MalformedResponseContext extends DownloadContext {
        override lazy val response = Future.value(ResponseBuilder.badRequest())
      }

      "An exception is thrown" in new MalformedResponseContext {
        result should throwAn[Exception]
      }
    }

    "when upstream request fails" >> {
      trait MalformedResponseContext extends DownloadContext {
        override lazy val response = Future.exception(new Exception)
      }

      "An exception is thrown" in new MalformedResponseContext {
        result should throwAn[Exception]
      }
    }
  }
}
