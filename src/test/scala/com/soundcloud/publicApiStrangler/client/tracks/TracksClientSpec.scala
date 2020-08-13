package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class TracksClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val userSession = mock[UserSession]
    val trackUrn = Urn("soundcloud", "tracks", "2")
    val client = new TracksClient(jsonClient)
  }

  "#streamUrl" >> {
    trait StreamRequestContext extends Context {
      val streamRequest = StreamRequest(trackUrn, None, "token", "hls")
    }

    "with successful response" >> {
      trait SuccessContext extends StreamRequestContext {
        //        val responseBody = ReadFixture.asString("tracks", "stream_response")
        val responseBody = Fixtures.tracksStreamResponse.toString()
        Fixtures.tracksStreamResponse
        val response = Future.value {
          val response = Response(Status.Ok)
          response.setContentString(responseBody)
          response
        }

        jsonClient.postWithSession(
          userSession,
          Path() / "track" / "stream",
          Params.empty,
          Headers.empty,
          Some(Json.stringify(Json.toJson(streamRequest)))
        ) returns response
      }

      "returns successful stream response" in new SuccessContext {
        Await.result(client.streamUrl(userSession, streamRequest)) ==== Json.parse(responseBody).as[StreamUrlResponse]
      }
    }

    "with unsuccessful response" >> {
      trait UnsuccessfulContext extends StreamRequestContext {
        def callClientWithErrorStatus(status: Status) {
          val response = Future.value(Response(status))
          jsonClient.postWithSession(
            userSession,
            Path() / "track" / "stream",
            Params.empty,
            Headers.empty,
            Some(Json.stringify(Json.toJson(streamRequest)))
          ) returns response
          Await.result(client.streamUrl(userSession, streamRequest))
        }
      }

      "returns an error on client error" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.InternalServerError) must throwA[UnhandledResponseException]
      }

      "returns MediaStreamsError when the client returns an unauthorized status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.Unauthorized) ==== StreamErrorResponse
      }

      "returns MediaStreamsError when the client returns a not found status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.NotFound) ==== StreamErrorResponse
      }
    }
  }

  "#previewUrl" >> {
    trait PreviewRequestContext extends Context {
      val streamRequest = StreamRequest(trackUrn, None, "token", "hls")
    }

    "with successful response" >> {
      trait SuccessContext extends PreviewRequestContext {
        val responseBody = Fixtures.tracksStreamResponse.toString()
        val response = Future.value {
          val response = Response(Status.Ok)
          response.setContentString(responseBody)
          response
        }

        jsonClient.postWithSession(
          userSession,
          Path() / "track" / "preview",
          Params.empty,
          Headers.empty,
          Some(Json.stringify(Json.toJson(streamRequest)))
        ) returns response
      }

      "returns successful preview stream" in new SuccessContext {
        Await.result(client.previewUrl(userSession, streamRequest)) ==== Json.parse(responseBody).as[StreamUrlResponse]
      }
    }

    "with unsuccessful response" >> {
      trait UnsuccessfulContext extends PreviewRequestContext {
        def callClientWithErrorStatus(status: Status) {
          val response = Future.value(Response(status))
          jsonClient.postWithSession(
            userSession,
            Path() / "track" / "preview",
            Params.empty,
            Headers.empty,
            Some(Json.stringify(Json.toJson(streamRequest)))
          ) returns response
          Await.result(client.previewUrl(userSession, streamRequest))
        }
      }

      "returns an error" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.InternalServerError) must throwA[UnhandledResponseException]
      }

      "returns MediaStreamsError when the client returns an unauthorized status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.Unauthorized) ==== StreamErrorResponse
      }

      "returns MediaStreamsError when the client returns a not found status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.NotFound) ==== StreamErrorResponse
      }
    }
  }

  "#downloadUrl" >> {
    trait DownloadRequestContext extends Context {
      val downloadRequest = DownloadRequest(trackUrn, None, skipLogging = false)
    }

    "with successful response" >> {
      trait SuccessContext extends DownloadRequestContext {
        val responseBody = Fixtures.tracksDownloadResponse.toString()
        val response = Future.value {
          val response = Response(Status.Ok)
          response.setContentString(responseBody)
          response
        }

        jsonClient.postWithSession(
          userSession,
          Path() / "track" / "download",
          Params.empty,
          Headers.empty,
          Some(Json.stringify(Json.toJson(downloadRequest)))
        ) returns response
      }

      "returns successful download url" in new SuccessContext {
        Await.result(client.downloadUrl(userSession, downloadRequest)) ==== Json
          .parse(responseBody)
          .as[DownloadUrlResponse]
      }
    }

    "with unsuccessful response" >> {
      trait UnsuccessfulContext extends DownloadRequestContext {
        def callClientWithErrorStatus(status: Status) {
          val response = Future.value(Response(status))
          jsonClient.postWithSession(
            userSession,
            Path() / "track" / "download",
            Params.empty,
            Headers.empty,
            Some(Json.stringify(Json.toJson(downloadRequest)))
          ) returns response
          Await.result(client.downloadUrl(userSession, downloadRequest))
        }
      }

      "returns an error" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.InternalServerError) must throwA[UnhandledResponseException]
      }

      "returns DownloadErrorResponse when the client returns an unauthorized status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.Forbidden) ==== DownloadErrorResponse
      }

      "returns DownloadErrorResponse when the client returns a not found status" in new UnsuccessfulContext {
        callClientWithErrorStatus(Status.NotFound) ==== DownloadErrorResponse
      }
    }
  }

  "error when a stream url request returns a errors" in new Context {

    trait StreamErrorContext extends Context {
      var status: Status = Status.NotFound
      val streamRequest = StreamRequest(trackUrn, None, "token", "hls")

      val response = Future.value {
        val response = Response(status)
        response.setContentString("")
        response
      }

      lazy val trackRequests = List(
        TrackRequest(Urn("soundcloud", "tracks", "522587247"), None),
        TrackRequest(Urn("soundcloud", "tracks", "522587248"), Some("token"))
      )

      jsonClient.postWithSession(
        userSession,
        Path() / "track" / "stream",
        Params.empty,
        Headers.empty,
        Some(Json.stringify(Json.toJson(TracksParams(trackRequests))))
      ) returns response
    }

    "returns 404" in new StreamErrorContext {
      status = Status.NotFound
      Await.result(client.streamUrl(userSession, streamRequest)) ==== StreamErrorResponse
    }

    "returns 500" in new StreamErrorContext {
      status = Status.InternalServerError
      Await.result(client.streamUrl(userSession, streamRequest)) should throwAn[Exception]
    }
  }
}
