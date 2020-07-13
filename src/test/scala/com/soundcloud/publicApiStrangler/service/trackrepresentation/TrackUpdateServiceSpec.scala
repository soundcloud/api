package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.google.protobuf.ByteString
import com.soundcloud.hocuspocus.{HocuspocusService, Image, Kind, Raw}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.outcome._
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackMetadataUpdateResult, TrackRequest}
import com.soundcloud.publicApiStrangler.handler.support.error.UnhandledOutcomeException
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateRequest,
  TrackArtworkUpdateResult,
  TrackAssetDataUpdateRequest,
  TrackMetadataUpdateRequest
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.Json

class TrackUpdateServiceSpec extends UnitSpecification with TrackRepresentationSpecContext {
  trait Context extends Scope {
    val trackCoordinatorClient = mock[TrackCoordinatorClient]
    val hocuspocusService = mock[HocuspocusService]
    val trackService = mock[TrackRepresentationsService]

    val mockTrackRepresentation = createTrackRepresentation()
    val ownerSession = new UserSessionBuilder().setUser(mockTrackRepresentation.user.urn).build

    val trackUpdateService = new TrackUpdateService(trackCoordinatorClient, hocuspocusService, trackService)

    def setupMocksForTrackService(trackUrn: Urn, expectedResponse: Option[TrackRepresentation]) = {
      when(trackService.track(ownerSession, new TrackRequest(trackUrn, None))).thenReturn(
        Future.value(expectedResponse)
      )
    }

    val mockTrackMetadataUpdateResult =
      TrackMetadataUpdateResult(
        urn = mockTrackRepresentation.track.urn.toString,
        public = mockTrackRepresentation.track.public,
        api_streamable = mockTrackRepresentation.track.api_streamable,
        commentable = mockTrackRepresentation.track.commentable,
        description = Some("changed"),
        title = "changed",
        downloadable = mockTrackRepresentation.track.downloadable,
        embeddable = mockTrackRepresentation.track.embeddable,
        genre = mockTrackRepresentation.track.genre,
        geo_blockings = Some(mockTrackRepresentation.geoblockings.get.toList),
        isrc = Some(mockTrackRepresentation.isrc.get.toString),
        label_name = mockTrackRepresentation.track.label_name,
        license = mockTrackRepresentation.track.license,
        permalink = mockTrackRepresentation.track.permalink,
        purchase_title = mockTrackRepresentation.track.purchase_title,
        purchase_url = mockTrackRepresentation.track.purchase_url,
        release_day = mockTrackRepresentation.track.release_day,
        release_month = mockTrackRepresentation.track.release_month,
        reveal_comments = mockTrackRepresentation.track.reveal_comments,
        reveal_stats = mockTrackRepresentation.track.reveal_stats,
        tag_list = Some(mockTrackRepresentation.track.user_tags.mkString(","))
      )

    def setupMocksForUpdateTrackMeta(
        trackUrn: Urn,
        updateTrackMetadata: Option[TrackMetadataUpdateRequest],
        artworkMetadata: Option[TrackArtworkUpdateResult],
        expectedResponse: Outcome[TrackMetadataUpdateResult]
    ) = {
      when(
        trackCoordinatorClient.updateTrack(
          ownerSession,
          trackUrn,
          updateTrackMetadata,
          artworkMetadata
        )
      ).thenReturn(Future.value(expectedResponse))
    }

    def setupMocksForUpdateTrackAsset(
        trackAssetDataUpdateRequest: TrackAssetDataUpdateRequest,
        trackUpdateOutcome: Outcome[Unit]
    ) = {
      when(
        trackCoordinatorClient.updateTrackAssetData(trackAssetDataUpdateRequest, ownerSession, trackUrn)
      ).thenReturn(Future.value(trackUpdateOutcome))
    }

    def setupMocksForHocusPocusService(
        maybeUpdateAlbumArt: TrackArtworkUpdateRequest
    ) = {
      when(
        hocuspocusService.storeImage(
          Raw(Kind.ARTWORKS, ByteString.copyFrom(maybeUpdateAlbumArt.imageData))
        )
      ).thenReturn(Future.value(Image(kind = Kind.ARTWORKS, originUri = "s3://bucket/filename")))
    }
  }

  "Update track metadata" >> {
    trait SuccessContext extends Context {
      val metadataUpdateParams = Map[String, String]("title" -> "changed", "description" -> "changed")
      val updateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams)

      val expectedResponse = mockTrackRepresentation.copy(track =
        mockTrackRepresentation.track.copy(title = "changed", description = Some("changed"))
      )
    }

    trait FailureContext extends Context {
      val metadataUpdateParams = Map[String, String]("title" -> "changed", "description" -> "changed")
      val updateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams)
    }

    "Successfully updates metadata on valid request" in new SuccessContext {
      setupMocksForUpdateTrackMeta(
        mockTrackRepresentation.track.urn,
        updateRequest,
        None,
        Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForTrackService(mockTrackRepresentation.track.urn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          None,
          None,
          updateRequest,
          trackUrn,
          ownerSession
        )
      )

      result match {
        case Good(track) =>
          Json.toJson(track) === Json.toJson(expectedResponse)
        case _ =>
      }
    }

    "Returns nothing if update call fails" in new FailureContext {
      setupMocksForUpdateTrackMeta(
        mockTrackRepresentation.track.urn,
        updateRequest,
        None,
        NotFound().bad
      )

      setupMocksForTrackService(mockTrackRepresentation.track.urn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          None,
          None,
          updateRequest,
          trackUrn,
          ownerSession
        )
      )

      result.isLeft
      result match {
        case Bad(NotFound(msg)) => msg === "Resource not found"
        case _ =>
      }
    }

    "Returns nothing if track fetch call fails" in new FailureContext {
      setupMocksForUpdateTrackMeta(
        mockTrackRepresentation.track.urn,
        updateRequest,
        None,
        Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForTrackService(mockTrackRepresentation.track.urn, None)

      Await.result(
        trackUpdateService.updateTrack(None, None, updateRequest, trackUrn, ownerSession)
      ) must throwAn[UnhandledOutcomeException]
    }
  }

  "Update track album artwork" >> {
    trait SuccessContent extends Context {
      val bytes = ByteArray("i-am-an-image".getBytes(): _*)
      val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = Buf.ByteArray.Owned.extract(bytes))
      val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")
      val trackMetadataRequest = TrackMetadataUpdateRequest.fromForm(
        Map[String, String]("title" -> "changed", "description" -> "changed")
      )

      val artworkUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.track.description,
        title = mockTrackRepresentation.track.title
      )

      val expectedResponse = mockTrackRepresentation.copy(track =
        mockTrackRepresentation.track.copy(title = "changed", description = Some("changed"))
      )
    }

    trait FailureContext extends Context {
      val bytes = ByteArray("i-am-an-image".getBytes(): _*)
      val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = Buf.ByteArray.Owned.extract(bytes))
      val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")

      val artworkUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.track.description,
        title = mockTrackRepresentation.track.title
      )
    }

    "Can update track and album data" in new SuccessContent {
      setupMocksForUpdateTrackMeta(
        trackUrn,
        trackMetadataRequest,
        Some(trackArtworkUpdateResult),
        Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, Some(expectedResponse))

      val result = Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          None,
          trackMetadataRequest,
          mockTrackRepresentation.track.urn,
          ownerSession
        )
      )

      result match {
        case Good(track) =>
          Json.toJson(track) === Json.toJson(expectedResponse)
        case _ =>
      }
    }

    "Can update only album data" in new SuccessContent {
      setupMocksForUpdateTrackMeta(
        trackUrn,
        None,
        Some(trackArtworkUpdateResult),
        Good(artworkUpdateOnlyUpdateResponse)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          None,
          None,
          mockTrackRepresentation.track.urn,
          ownerSession
        )
      )

      result match {
        case Good(track) =>
          Json.toJson(track) === Json.toJson(mockTrackRepresentation)
        case _ =>
      }
    }

    "Returns error if track does not exist" in new FailureContext {
      setupMocksForUpdateTrackMeta(
        trackUrn,
        None,
        Some(trackArtworkUpdateResult),
        Good(artworkUpdateOnlyUpdateResponse)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, None)

      Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          None,
          None,
          mockTrackRepresentation.track.urn,
          ownerSession
        )
      ) must throwAn[UnhandledOutcomeException]
    }
  }

  "update track asset data" >> {
    trait SuccessContext extends Context {
      val trackAssetDataUpdateRequest = TrackAssetDataUpdateRequest(
        replacing_original_filename = "replacing-filename",
        replacing_uid = "replacing-uid"
      )

      val assetUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.track.description,
        title = mockTrackRepresentation.track.title
      )
    }

    trait FailureContext extends Context {
      val trackAssetDataUpdateRequest = TrackAssetDataUpdateRequest(
        replacing_original_filename = "invalid-filename",
        replacing_uid = "invalid-uid"
      )

      val assetUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.track.description,
        title = mockTrackRepresentation.track.title
      )
    }

    "Successfully updates asset data" in new SuccessContext {
      setupMocksForUpdateTrackAsset(trackAssetDataUpdateRequest, Good(()))
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(trackUrn, None, None, Good(assetUpdateOnlyUpdateResponse))

      val result = Await.result(
        trackUpdateService.updateTrack(
          None,
          Some(trackAssetDataUpdateRequest),
          None,
          trackUrn,
          ownerSession
        )
      )

      result match {
        case Good(track) =>
          Json.toJson(track) === Json.toJson(mockTrackRepresentation)
        case _ =>
      }
    }

    "returns 404 if track asset data not found" in new FailureContext {
      setupMocksForUpdateTrackAsset(trackAssetDataUpdateRequest, NotFound().bad)
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(trackUrn, None, None, Good(assetUpdateOnlyUpdateResponse))

      val result = Await.result(
        trackUpdateService.updateTrack(
          None,
          Some(trackAssetDataUpdateRequest),
          None,
          trackUrn,
          ownerSession
        )
      )

      result.isLeft
      result match {
        case Bad(NotFound(msg)) => msg === "Resource not found"
        case _ =>
      }
    }

    "returns 400 if request was invalid" in new FailureContext {
      setupMocksForUpdateTrackAsset(trackAssetDataUpdateRequest, NotValid("invalid request").bad)
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(trackUrn, None, None, Good(assetUpdateOnlyUpdateResponse))

      val result = Await.result(
        trackUpdateService.updateTrack(
          None,
          Some(trackAssetDataUpdateRequest),
          None,
          trackUrn,
          ownerSession
        )
      )

      result.isLeft
      result match {
        case Bad(NotFound(msg)) => msg === "invalid request"
        case _ =>
      }
    }
  }
}
