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
}
