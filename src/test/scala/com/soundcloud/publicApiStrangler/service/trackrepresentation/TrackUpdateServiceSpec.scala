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
        urn = mockTrackRepresentation.visibleTrack.urn.toString,
        public = mockTrackRepresentation.visibleTrack.public,
        api_streamable = mockTrackRepresentation.visibleTrack.apiStreamable,
        commentable = mockTrackRepresentation.visibleTrack.commentable,
        description = Some("changed"),
        title = "changed",
        downloadable = Some(mockTrackRepresentation.visibleTrack.downloadable),
        embeddable = mockTrackRepresentation.visibleTrack.embeddable,
        genre = mockTrackRepresentation.visibleTrack.genre,
        geo_blockings = Some(mockTrackRepresentation.geoblockings.get.toList),
        isrc = Some(mockTrackRepresentation.isrc.get.toString),
        label_name = mockTrackRepresentation.visibleTrack.labelName,
        license = mockTrackRepresentation.visibleTrack.license,
        permalink = mockTrackRepresentation.visibleTrack.permalink,
        purchase_title = mockTrackRepresentation.visibleTrack.purchaseTitle,
        purchase_url = mockTrackRepresentation.visibleTrack.purchaseUrl,
        release_day = mockTrackRepresentation.visibleTrack.releaseDay,
        release_month = mockTrackRepresentation.visibleTrack.releaseMonth,
        reveal_comments = mockTrackRepresentation.visibleTrack.revealComments,
        reveal_stats = mockTrackRepresentation.visibleTrack.revealStats,
        tag_list = Some(mockTrackRepresentation.visibleTrack.userTags.mkString(","))
      )

    def setupMocksForUpdateTrackMeta(
        trackUrn: Urn,
        trackAssetDataUpdateRequest: Option[TrackAssetDataUpdateRequest],
        updateTrackMetadata: Option[TrackMetadataUpdateRequest],
        artworkMetadata: Option[TrackArtworkUpdateResult],
        expectedResponse: Outcome[TrackMetadataUpdateResult]
    ) = {
      when(
        trackCoordinatorClient.updateTrack(
          ownerSession,
          trackUrn,
          trackAssetDataUpdateRequest,
          updateTrackMetadata,
          artworkMetadata
        )
      ).thenReturn(Future.value(expectedResponse))
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
      val metaDataUpdateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams)
      val trackAssetDataUpdateRequest =
        TrackAssetDataUpdateRequest(replacing_original_filename = "filename", replacing_uid = "uid")

      val expectedResponse = mockTrackRepresentation.copy(visibleTrack =
        mockTrackRepresentation.visibleTrack.copy(title = "changed", description = Some("changed"))
      )
    }

    trait FailureContext extends Context {
      val metadataUpdateParams = Map[String, String]("title" -> "changed", "description" -> "changed")
      val updateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams)
    }

    "Successfully updates metadata on valid request" in new SuccessContext {
      setupMocksForUpdateTrackMeta(
        trackUrn = mockTrackRepresentation.visibleTrack.urn,
        trackAssetDataUpdateRequest = None,
        updateTrackMetadata = metaDataUpdateRequest,
        artworkMetadata = None,
        expectedResponse = Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForTrackService(mockTrackRepresentation.visibleTrack.urn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          maybeUpdateAlbumArt = None,
          maybeUpdateTrackAsset = None,
          metaDataUpdateRequest,
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
        mockTrackRepresentation.visibleTrack.urn,
        trackAssetDataUpdateRequest = None,
        updateRequest,
        artworkMetadata = None,
        NotFound().bad
      )

      setupMocksForTrackService(mockTrackRepresentation.visibleTrack.urn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          maybeUpdateAlbumArt = None,
          maybeUpdateTrackAsset = None,
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
        mockTrackRepresentation.visibleTrack.urn,
        trackAssetDataUpdateRequest = None,
        updateRequest,
        artworkMetadata = None,
        Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForTrackService(mockTrackRepresentation.visibleTrack.urn, None)

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
        description = mockTrackRepresentation.visibleTrack.description,
        title = mockTrackRepresentation.visibleTrack.title
      )

      val expectedResponse = mockTrackRepresentation.copy(visibleTrack =
        mockTrackRepresentation.visibleTrack.copy(title = "changed", description = Some("changed"))
      )
    }

    trait FailureContext extends Context {
      val bytes = ByteArray("i-am-an-image".getBytes(): _*)
      val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = Buf.ByteArray.Owned.extract(bytes))
      val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")

      val artworkUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.visibleTrack.description,
        title = mockTrackRepresentation.visibleTrack.title
      )
    }

    "Can update track and album data" in new SuccessContent {
      setupMocksForUpdateTrackMeta(
        trackUrn,
        trackAssetDataUpdateRequest = None,
        trackMetadataRequest,
        Some(trackArtworkUpdateResult),
        Good(mockTrackMetadataUpdateResult)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, Some(expectedResponse))

      val result = Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          maybeUpdateTrackAsset = None,
          trackMetadataRequest,
          mockTrackRepresentation.visibleTrack.urn,
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
        trackAssetDataUpdateRequest = None,
        updateTrackMetadata = None,
        Some(trackArtworkUpdateResult),
        Good(artworkUpdateOnlyUpdateResponse)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))

      val result = Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          maybeUpdateTrackAsset = None,
          maybeTrackMetadata = None,
          mockTrackRepresentation.visibleTrack.urn,
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
        trackAssetDataUpdateRequest = None,
        updateTrackMetadata = None,
        Some(trackArtworkUpdateResult),
        Good(artworkUpdateOnlyUpdateResponse)
      )

      setupMocksForHocusPocusService(trackArtworkMetaRequest)
      setupMocksForTrackService(trackUrn, None)

      Await.result(
        trackUpdateService.updateTrack(
          Some(trackArtworkMetaRequest),
          maybeUpdateTrackAsset = None,
          maybeTrackMetadata = None,
          mockTrackRepresentation.visibleTrack.urn,
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
        description = mockTrackRepresentation.visibleTrack.description,
        title = mockTrackRepresentation.visibleTrack.title
      )
    }

    trait FailureContext extends Context {
      val trackAssetDataUpdateRequest = TrackAssetDataUpdateRequest(
        replacing_original_filename = "invalid-filename",
        replacing_uid = "invalid-uid"
      )

      val assetUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
        description = mockTrackRepresentation.visibleTrack.description,
        title = mockTrackRepresentation.visibleTrack.title
      )
    }

    "Successfully updates asset data" in new SuccessContext {
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(
        trackUrn,
        Some(trackAssetDataUpdateRequest),
        updateTrackMetadata = None,
        artworkMetadata = None,
        Good(assetUpdateOnlyUpdateResponse)
      )

      val result = Await.result(
        trackUpdateService.updateTrack(
          maybeUpdateAlbumArt = None,
          Some(trackAssetDataUpdateRequest),
          maybeTrackMetadata = None,
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
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(trackUrn, Some(trackAssetDataUpdateRequest), None, None, NotFound().bad)

      val result = Await.result(
        trackUpdateService.updateTrack(
          maybeUpdateAlbumArt = None,
          Some(trackAssetDataUpdateRequest),
          maybeTrackMetadata = None,
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
      setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
      setupMocksForUpdateTrackMeta(
        trackUrn = trackUrn,
        Some(trackAssetDataUpdateRequest),
        updateTrackMetadata = None,
        artworkMetadata = None,
        NotValid("invalid request").bad
      )

      val result = Await.result(
        trackUpdateService.updateTrack(
          maybeUpdateAlbumArt = None,
          Some(trackAssetDataUpdateRequest),
          maybeTrackMetadata = None,
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
