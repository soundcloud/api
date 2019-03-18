package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.Status
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

  "fetchTranscodings" >> {
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

  "fetchStreamUrl" >> {
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

  "fetchPreviewUrl" >> {
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
}
