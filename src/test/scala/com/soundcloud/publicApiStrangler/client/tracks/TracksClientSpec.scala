package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  MonetizationModel,
  Reason
}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsNull, Json}

class TracksClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val userSession = mock[UserSession]
    val client = new TracksClient(jsonClient)

    val trackUrn = Urn("soundcloud", "tracks", "2")
    val downloadUrl = "https://cf-hls-media.sndcdn.com/download/track2"
    val userUrn = Urn("soundcloud", "users", "15777")
    val trackUid = Some("NnPYWvWwB6ln")
    val apiStreamable = Some(true)
    val downloadable = true
    val disabledAt = None
    val contentAuth =
      new ContentAuthorization(trackUrn, ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
  }

  "visibleTrack" >> {
    "track service returns a track" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(
          List(
            Json.obj("urn" -> trackUrn.toString)
          )
        )
      )
      jsonClient.postWithSession(
        userSession,
        Path() / "tracks",
        Params.empty,
        Headers.empty,
        Some(Json.stringify(jsonBody))
      ) returns
        Future.value(jsonResponse(Status.Ok, withContentsOf("tracks", "visible_track")))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) ==== Some(
        VisibleTrack(trackUrn, userUrn, trackUid, apiStreamable, downloadable, disabledAt, contentAuth)
      )
    }

    "track service returns no track" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(
          List(
            Json.obj("urn" -> trackUrn.toString)
          )
        )
      )
      jsonClient.postWithSession(
        userSession,
        Path() / "tracks",
        Params.empty,
        Headers.empty,
        Some(Json.stringify(jsonBody))
      ) returns
        Future.value(jsonResponse(Status.Ok, Json.obj("data" -> JsArray())))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) ==== None
    }

    "track service returns a 500" in new Context {
      val jsonBody = Json.obj(
        "trackRequests" -> JsArray(
          List(
            Json.obj("urn" -> trackUrn.toString)
          )
        )
      )
      jsonClient.postWithSession(
        userSession,
        Path() / "tracks",
        Params.empty,
        Headers.empty,
        Some(Json.stringify(jsonBody))
      ) returns
        Future.value(jsonResponse(Status.InternalServerError, JsNull))
      Await.result(client.visibleTrack(userSession, trackUrn, None)) should throwAn[UnhandledResponseException]
    }
  }

  "downloadUrl" >> {
    trait DownloadRequestContext extends Context {
      val downloadRequest = DownloadRequest(trackUrn, None, skipLogging = false)
    }

    "with successful response" >> {
      trait SuccessContext extends DownloadRequestContext {
        jsonClient.postWithSession(
          userSession,
          Path() / "track" / "download",
          Params.empty,
          Headers.empty,
          Some(Json.stringify(Json.toJson(downloadRequest)))
        ) returns
          Future.value(jsonResponse(Status.Ok, withContentsOf("tracks", "download_response")))
      }

      "returns successful download url" in new SuccessContext {
        Await.result(client.downloadUrl(userSession, downloadRequest)) ==== DownloadUrlResponse(downloadUrl)
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
