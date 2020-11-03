package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateResult,
  TrackAssetDataCreateRequest,
  TrackAssetDataUpdateRequest,
  TrackMetadataUpdateRequest
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.finagle.http.Status
import com.twitter.io.Buf.ByteArray
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.{JsNull, JsObject, Json}

class TrackCoordinatorClientSpec extends UnitSpecification {
  trait Context extends Scope {
    val jsonClient = mock[JsonClient]
    val client = new TrackCoordinatorClient(jsonClient)

    val userUrn = Urn("soundcloud", "users", "123")
    val trackUrn = Urn("soundcloud", "tracks", "123")
    val session = new UserSessionBuilder().setUser(userUrn).build

    val trackMetadataUpdateRequest = TrackMetadataUpdateRequest
      .fromForm(
        Map[String, String]("title" -> "changed", "description" -> "changed")
      )
      .get

    val bytes = ByteArray("i-am-an-image".getBytes(): _*)
    val trackArtworkMetaResponse = TrackArtworkUpdateResult("bucket", "filename")
    val trackAssetDataUpdateRequest =
      TrackAssetDataUpdateRequest(replacing_original_filename = "filename", replacing_uid = "uid")

    val trackAssetDataCreateRequest =
      TrackAssetDataCreateRequest(original_filename = "filename", uid = "uid")

    val expectedResponse = Json.parse(Json.stringify(Fixtures.trackCoordinatorTrack)).as[TrackCoordinatorTrack]
  }

  trait CreateContext extends Context {
    val path = Path("/tracks")

    val requestBody = Json.stringify(
      Json.toJson(trackAssetDataCreateRequest).as[JsObject] ++
        Json.toJson(trackMetadataUpdateRequest.track).as[JsObject] ++
        Json.obj(
          "artwork_from_s3" -> Json.toJson(trackArtworkMetaResponse)
        )
    )
  }

  trait UpdateContext extends Context {
    val path = Path("/tracks") / trackUrn

    val requestBody = Json.stringify(
      Json.toJson(trackAssetDataUpdateRequest).as[JsObject] ++
        Json.toJson(trackMetadataUpdateRequest.track).as[JsObject] ++
        Json.obj(
          "artwork_from_s3" -> Json.toJson(trackArtworkMetaResponse)
        )
    )
  }

  "#createTrack" >> {
    "metadata" >> {
      trait SuccessContext extends CreateContext {
        when(jsonClient.postWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.Created, Fixtures.trackCoordinatorTrack))
          )
      }

      trait NotFoundContext extends CreateContext {
        when(jsonClient.postWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.NotFound, JsNull))
          )
      }
      trait ErrorContext extends CreateContext {
        when(jsonClient.postWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
          )
      }

      "Successfully creates track metadata" in new SuccessContext {
        val result =
          Await.result(
            client.createTrack(
              session,
              trackAssetDataCreateRequest,
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual Good(expectedResponse)
      }

      "Returns 404 when not found" in new NotFoundContext {
        val result =
          Await.result(
            client.createTrack(
              session,
              trackAssetDataCreateRequest,
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual NotFound().bad
      }

      "Handles unexpected error" in new ErrorContext {
        Await.result(
          client.createTrack(
            session,
            trackAssetDataCreateRequest,
            Some(trackMetadataUpdateRequest),
            Some(trackArtworkMetaResponse)
          )
        ) must throwAn[UnhandledResponseException]
      }
    }

    "Asset data" >> {
      trait SuccessContext extends CreateContext {

        when(
          jsonClient.postWithSession(
            session,
            Path("/tracks"),
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.Created, Fixtures.trackCoordinatorTrack))
        )
      }

      trait NotFoundContext extends CreateContext {
        when(
          jsonClient.postWithSession(
            session,
            Path("/tracks"),
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.NotFound, JsNull))
        )
      }

      trait ErrorContext extends CreateContext {
        when(
          jsonClient.postWithSession(
            session,
            Path("/tracks"),
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
        )
      }

      "Successfully creates track audio data" in new SuccessContext {
        val result =
          Await.result(
            client.createTrack(
              session,
              trackAssetDataCreateRequest,
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual Good(expectedResponse)
      }

      "Return 404 if track not found" in new NotFoundContext {
        val result =
          Await.result(
            client.createTrack(
              session,
              trackAssetDataCreateRequest,
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual NotFound().bad
      }

      "Handles unexpected error" in new ErrorContext {
        Await.result(
          client.createTrack(
            session,
            trackAssetDataCreateRequest,
            Some(trackMetadataUpdateRequest),
            Some(trackArtworkMetaResponse)
          )
        ) must throwAn[UnhandledResponseException]
      }
    }
  }

  "#updateTrack" >> {
    "metadata" >> {
      trait SuccessContext extends UpdateContext {
        when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.Ok, Fixtures.trackCoordinatorTrack))
          )
      }

      trait NotFoundContext extends UpdateContext {
        when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.NotFound, JsNull))
          )
      }
      trait ErrorContext extends UpdateContext {
        when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
          .thenReturn(
            Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
          )
      }

      "Successfully update track metadata" in new SuccessContext {
        val result =
          Await.result(
            client.updateTrack(
              session,
              trackUrn,
              Some(trackAssetDataUpdateRequest),
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual Good(expectedResponse)
      }

      "Update metadata returns 404 when not found" in new NotFoundContext {
        val result =
          Await.result(
            client.updateTrack(
              session,
              trackUrn,
              Some(trackAssetDataUpdateRequest),
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual NotFound().bad
      }

      "Handles unexpected error" in new ErrorContext {
        Await.result(
          client.updateTrack(
            session,
            trackUrn,
            Some(trackAssetDataUpdateRequest),
            Some(trackMetadataUpdateRequest),
            Some(trackArtworkMetaResponse)
          )
        ) must throwAn[UnhandledResponseException]
      }
    }

    "asset data" >> {
      trait SuccessContext extends UpdateContext {

        when(
          jsonClient.putWithSession(
            session,
            Path("/tracks") / trackUrn,
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.Ok, Fixtures.trackCoordinatorTrack))
        )
      }

      trait NotFoundContext extends UpdateContext {
        when(
          jsonClient.putWithSession(
            session,
            Path("/tracks") / trackUrn,
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.NotFound, JsNull))
        )
      }

      trait ErrorContext extends UpdateContext {
        when(
          jsonClient.putWithSession(
            session,
            Path("/tracks") / trackUrn,
            Params.empty,
            Headers.empty,
            Some(requestBody)
          )
        ).thenReturn(
          Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
        )
      }

      "Successfully update track audio data" in new SuccessContext {
        val result =
          Await.result(
            client.updateTrack(
              session,
              trackUrn,
              Some(trackAssetDataUpdateRequest),
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual Good(expectedResponse)
      }

      "Return 404 if track not found" in new NotFoundContext {
        val result =
          Await.result(
            client.updateTrack(
              session,
              trackUrn,
              Some(trackAssetDataUpdateRequest),
              Some(trackMetadataUpdateRequest),
              Some(trackArtworkMetaResponse)
            )
          )

        result mustEqual NotFound().bad
      }

      "Handles unexpected error" in new ErrorContext {
        Await.result(
          client.updateTrack(
            session,
            trackUrn,
            Some(trackAssetDataUpdateRequest),
            Some(trackMetadataUpdateRequest),
            Some(trackArtworkMetaResponse)
          )
        ) must throwAn[UnhandledResponseException]
      }
    }
  }

}
