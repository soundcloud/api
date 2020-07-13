package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.client.tracks.TrackMetadataUpdateResult
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateResult,
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

    val path = Path("/tracks") / trackUrn

    val requestBody = Json.stringify(
      Json.toJson(trackMetadataUpdateRequest.track).as[JsObject] ++ Json.obj(
        "artwork_from_s3" -> Json.toJson(trackArtworkMetaResponse)
      )
    )

    val trackAssetUpdateRequestBody = Json.stringify(Json.toJson(trackAssetDataUpdateRequest))
  }

  "Update track metadata" >> {
    trait SuccessContext extends Context {
      val expectedResponse = TrackMetadataUpdateResult(
        urn = "soundcloud:sounds:174088262",
        public = true,
        title = "Awesome Track",
        api_streamable = Some(true),
        commentable = true,
        description = Some("This track is awesome"),
        downloadable = Some(false),
        embeddable = Some(true),
        genre = Some("Free jazz"),
        geo_blockings = Some(List("US")),
        isrc = Some("US-S1Z-99-00001"),
        label_name = Some("Foobar records"),
        license = "all-rights-reserved",
        permalink = "awesome-track-2014-10-27-17-25-29-66",
        purchase_title = Some("buy123"),
        purchase_url = Some("http://buy.that.com"),
        release_day = Some(1),
        release_month = Some(2),
        reveal_comments = true,
        reveal_stats = true,
        tag_list = Some("tag onw two \"hello tag\" tōkyō")
      )

      when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
        .thenReturn(
          Future(jsonResponse(Status.Ok, Fixtures.trackCoordinatorTrack))
        )
    }

    trait NotFoundContext extends Context {
      when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
        .thenReturn(
          Future(jsonResponse(Status.NotFound, JsNull))
        )
    }
    trait ErrorContext extends Context {
      when(jsonClient.putWithSession(session, path, Params.empty, Headers.empty, Some(requestBody)))
        .thenReturn(
          Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
        )
    }

    "Successfully update track metadata" in new SuccessContext {
      val result =
        Await.result(
          client.updateTrack(session, trackUrn, Some(trackMetadataUpdateRequest), Some(trackArtworkMetaResponse))
        )

      result mustEqual Good(expectedResponse)
    }

    "Update metadata returns 404 when not found" in new NotFoundContext {
      val result =
        Await.result(
          client.updateTrack(session, trackUrn, Some(trackMetadataUpdateRequest), Some(trackArtworkMetaResponse))
        )

      result mustEqual NotFound().bad
    }

    "Handles unexpected error" in new ErrorContext {
      Await.result(
        client.updateTrack(session, trackUrn, Some(trackMetadataUpdateRequest), Some(trackArtworkMetaResponse))
      ) must throwAn[UnhandledResponseException]
    }
  }

  "Update track asset data" >> {
    trait SuccessContext extends Context {
      when(
        jsonClient.putWithSession(
          session,
          Path("/tracks") / trackUrn,
          Params.empty,
          Headers.empty,
          Some(trackAssetUpdateRequestBody)
        )
      ).thenReturn(
        Future(jsonResponse(Status.Ok, JsNull))
      )
    }

    trait NotFoundContext extends Context {
      when(
        jsonClient.putWithSession(
          session,
          Path("/tracks") / trackUrn,
          Params.empty,
          Headers.empty,
          Some(trackAssetUpdateRequestBody)
        )
      ).thenReturn(
        Future(jsonResponse(Status.NotFound, JsNull))
      )
    }

    trait ErrorContext extends Context {
      when(
        jsonClient.putWithSession(
          session,
          Path("/tracks") / trackUrn,
          Params.empty,
          Headers.empty,
          Some(trackAssetUpdateRequestBody)
        )
      ).thenReturn(
        Future(jsonResponse(Status.InternalServerError, Json.obj("400" -> "Invalid Request")))
      )
    }

    "Successfully update track audio data" in new SuccessContext {
      val result =
        Await.result(
          client.updateTrackAssetData(trackAssetDataUpdateRequest, session, trackUrn)
        )

      result mustEqual Good(())
    }

    "Return 404 if track not found" in new NotFoundContext {
      val result =
        Await.result(
          client.updateTrackAssetData(trackAssetDataUpdateRequest, session, trackUrn)
        )

      result mustEqual NotFound().bad
    }

    "Handles unexpected error" in new ErrorContext {
      Await.result(
        client.updateTrackAssetData(trackAssetDataUpdateRequest, session, trackUrn)
      ) must throwAn[UnhandledResponseException]
    }
  }
}
