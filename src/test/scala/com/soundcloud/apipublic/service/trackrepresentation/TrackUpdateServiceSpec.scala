package com.soundcloud.apipublic.service.trackrepresentation

import com.google.protobuf.ByteString
import com.soundcloud.apipublic.client.mothership.OkidokiClient
import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.trackcoordinator.{
  TrackCoordinatorClient,
  TrackCoordinatorTrack,
  TrackCoordinatorTrackFixtures
}
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.handler.support.error.UnhandledOutcomeException
import com.soundcloud.apipublic.handler.support.requestParser._
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import com.soundcloud.hocuspocus.{HocuspocusService, Image, Kind, Raw}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.twitter.io.Buf
import com.twitter.io.Buf.ByteArray
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsObject, Json}

class TrackUpdateServiceSpec extends UnitSpecification with TrackRepresentationSpecContext {

  trait Context extends Scope {
    val trackCoordinatorClient = mock[TrackCoordinatorClient]
    val okidokiClient = mock[OkidokiClient]
    val hocuspocusService = mock[HocuspocusService]
    val trackService = mock[TrackRepresentationsService]

    val mockTrackRepresentation = createTrackRepresentationFromVisibleTrack()
    val ownerSession = new UserSessionBuilder().setUser(mockTrackRepresentation.user.urn).build

    val trackUpdateService =
      new TrackUpdateService(trackCoordinatorClient, okidokiClient, hocuspocusService, trackService)

    def setupMocksForTrackService(trackUrn: Urn, expectedResponse: Option[TrackRepresentation]) = {
      when(trackService.track(ownerSession, TrackRequest(trackUrn, None))).thenReturn(
        Future.value(expectedResponse)
      )
    }

    val mockTrackMetadataUpdateResult =
      new TrackCoordinatorTrackFixtures().fromTrackRepresentation(mockTrackRepresentation)

    val emptyTrackUpdate = TrackMetadataUpdateRequest.fromForm(Map.empty).getOrElse(null)

    def setupMocksForUpdateTrackMeta(
        trackUrn: Urn,
        trackAssetDataUpdateRequest: Option[TrackAssetDataUpdateRequest],
        updateTrackMetadata: TrackMetadataUpdateRequest,
        artworkMetadata: Option[TrackArtworkUpdateResult],
        expectedResponse: Outcome[TrackCoordinatorTrack]
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
          Raw(
            Kind.ARTWORKS,
            ByteString.copyFrom(Buf.ByteArray.Owned.extract(maybeUpdateAlbumArt.imageData)),
            Some(ownerSession.asProtoSession)
          )
        )
      ).thenReturn(Future.value(Image(kind = Kind.ARTWORKS, originUri = "s3://bucket/filename")))
    }
  }

  "#updateTrack" >> {
    "metadata" >> {
      trait SuccessContext extends Context {
        val metadataUpdateParams = Map[String, String]("title" -> "changed", "description" -> "changed")
        val metaDataUpdateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams).getOrElse(null)

        val expectedResponse = mockTrackRepresentation.copy(title = "changed", description = Some("changed"))
      }

      trait FailureContext extends Context {
        val metadataUpdateParams = Map[String, String]("title" -> "changed", "description" -> "changed")
        val updateRequest = TrackMetadataUpdateRequest.fromForm(metadataUpdateParams).getOrElse(null)
      }

      "Successfully updates metadata on valid request" in new SuccessContext {
        setupMocksForUpdateTrackMeta(
          trackUrn = mockTrackRepresentation.urn,
          trackAssetDataUpdateRequest = None,
          updateTrackMetadata = metaDataUpdateRequest,
          artworkMetadata = None,
          expectedResponse = Good(mockTrackMetadataUpdateResult)
        )

        setupMocksForTrackService(mockTrackRepresentation.urn, Some(mockTrackRepresentation))

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
          mockTrackRepresentation.urn,
          trackAssetDataUpdateRequest = None,
          updateRequest,
          artworkMetadata = None,
          NotFound().bad
        )

        setupMocksForTrackService(mockTrackRepresentation.urn, Some(mockTrackRepresentation))

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
          mockTrackRepresentation.urn,
          trackAssetDataUpdateRequest = None,
          updateRequest,
          artworkMetadata = None,
          Good(mockTrackMetadataUpdateResult)
        )

        setupMocksForTrackService(mockTrackRepresentation.urn, None)

        Await.result(
          trackUpdateService.updateTrack(None, None, updateRequest, trackUrn, ownerSession)
        ) must throwAn[UnhandledOutcomeException]
      }
    }

    "album artwork" >> {
      trait SuccessContent extends Context {
        val bytes = ByteArray("i-am-an-image".getBytes(): _*)
        val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = bytes)
        val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")
        val trackMetadataRequest = TrackMetadataUpdateRequest
          .fromForm(
            Map[String, String]("title" -> "changed", "description" -> "changed")
          )
          .getOrElse(null)

        val artworkUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
          description = mockTrackRepresentation.description,
          title = mockTrackRepresentation.title
        )

        val expectedResponse = mockTrackRepresentation.copy(title = "changed", description = Some("changed"))
      }

      trait FailureContext extends Context {
        val bytes = ByteArray("i-am-an-image".getBytes(): _*)
        val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = bytes)
        val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")

        val artworkUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
          description = mockTrackRepresentation.description,
          title = mockTrackRepresentation.title
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
            mockTrackRepresentation.urn,
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
          updateTrackMetadata = emptyTrackUpdate,
          Some(trackArtworkUpdateResult),
          Good(artworkUpdateOnlyUpdateResponse)
        )

        setupMocksForHocusPocusService(trackArtworkMetaRequest)
        setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))

        val result = Await.result(
          trackUpdateService.updateTrack(
            Some(trackArtworkMetaRequest),
            maybeUpdateTrackAsset = None,
            trackMetadata = emptyTrackUpdate,
            mockTrackRepresentation.urn,
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
          updateTrackMetadata = emptyTrackUpdate,
          Some(trackArtworkUpdateResult),
          Good(artworkUpdateOnlyUpdateResponse)
        )

        setupMocksForHocusPocusService(trackArtworkMetaRequest)
        setupMocksForTrackService(trackUrn, None)

        Await.result(
          trackUpdateService.updateTrack(
            Some(trackArtworkMetaRequest),
            maybeUpdateTrackAsset = None,
            trackMetadata = emptyTrackUpdate,
            mockTrackRepresentation.urn,
            ownerSession
          )
        ) must throwAn[UnhandledOutcomeException]
      }
    }

    "asset data" >> {
      trait SuccessContext extends Context {
        val trackAssetDataUpdateRequest = TrackAssetDataUpdateRequest(
          replacing_original_filename = "replacing-filename",
          replacing_uid = "replacing-uid"
        )

        val assetUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
          description = mockTrackRepresentation.description,
          title = mockTrackRepresentation.title
        )
      }

      trait FailureContext extends Context {
        val trackAssetDataUpdateRequest = TrackAssetDataUpdateRequest(
          replacing_original_filename = "invalid-filename",
          replacing_uid = "invalid-uid"
        )

        val assetUpdateOnlyUpdateResponse = mockTrackMetadataUpdateResult.copy(
          description = mockTrackRepresentation.description,
          title = mockTrackRepresentation.title
        )
      }

      "Successfully updates asset data" in new SuccessContext {
        setupMocksForTrackService(trackUrn, Some(mockTrackRepresentation))
        setupMocksForUpdateTrackMeta(
          trackUrn,
          Some(trackAssetDataUpdateRequest),
          updateTrackMetadata = emptyTrackUpdate,
          artworkMetadata = None,
          Good(assetUpdateOnlyUpdateResponse)
        )

        val result = Await.result(
          trackUpdateService.updateTrack(
            maybeUpdateAlbumArt = None,
            Some(trackAssetDataUpdateRequest),
            trackMetadata = emptyTrackUpdate,
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
        setupMocksForUpdateTrackMeta(
          trackUrn,
          Some(trackAssetDataUpdateRequest),
          emptyTrackUpdate,
          None,
          NotFound().bad
        )

        val result = Await.result(
          trackUpdateService.updateTrack(
            maybeUpdateAlbumArt = None,
            Some(trackAssetDataUpdateRequest),
            trackMetadata = emptyTrackUpdate,
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
          updateTrackMetadata = emptyTrackUpdate,
          artworkMetadata = None,
          NotValid("invalid request").bad
        )

        val result = Await.result(
          trackUpdateService.updateTrack(
            maybeUpdateAlbumArt = None,
            Some(trackAssetDataUpdateRequest),
            trackMetadata = emptyTrackUpdate,
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

  "#createTrack" >> {

    trait CreateTrackContext extends Context {
      def stubTrackCoordinatorClient(
          trackAsset: TrackAssetDataCreateRequest,
          trackMetadata: TrackMetadataCreateRequest,
          trackArtwork: Option[TrackArtworkUpdateResult],
          expectedResponse: Outcome[TrackCoordinatorTrack]
      ) = {
        when(
          trackCoordinatorClient.createTrack(
            ownerSession,
            trackAsset,
            trackMetadata,
            trackArtwork
          )
        ).thenReturn(Future.value(expectedResponse))
      }

      def stubOkidokiClient(
          expectedResponse: List[UserRepresentation]
      ) = {
        when(
          okidokiClient.fetchUserObjects(
            ownerSession,
            Set(userUrn)
          )
        ).thenReturn(Future.value(expectedResponse))
      }

      val metadataUpdateParams = Map[String, String]("title" -> "the title", "description" -> "the description")
      val metaDataUpdateRequest = TrackMetadataCreateRequest.fromForm(metadataUpdateParams).getOrElse(null)
      val trackAssetDataCreateRequest = TrackAssetDataCreateRequest(original_filename = "filename", uid = "uid")
      val trackCoordinatorTrack = Fixtures.trackCoordinatorTrack.as[TrackCoordinatorTrack]
      val trackArtworkUpdateResult = TrackArtworkUpdateResult(bucket = "bucket", filename = "filename")
      val bytes = ByteArray("i-am-an-image".getBytes(): _*)
      val trackArtworkMetaRequest = TrackArtworkUpdateRequest(imageData = bytes)
      val users = Fixtures.okidokiUsers.as[List[JsObject]].map(UserRepresentationMapper(_))
      val user = users.head
      val expectedResponse = TrackRepresentationBuilder.fromTrackCoordinatorTrack(trackCoordinatorTrack, user, None)
      setupMocksForHocusPocusService(trackArtworkMetaRequest)
    }

    "When track coordinator and okidoki user fetch succeeds" >> {

      trait SuccessContext extends CreateTrackContext {
        stubOkidokiClient(users)

        stubTrackCoordinatorClient(
          trackAssetDataCreateRequest,
          metaDataUpdateRequest,
          Some(trackArtworkUpdateResult),
          Good(trackCoordinatorTrack)
        )
      }

      "Returns TrackRepresentation" in new SuccessContext {

        val result = Await.result(
          trackUpdateService.createTrack(
            trackAsset = trackAssetDataCreateRequest,
            maybeAlbumArt = Some(trackArtworkMetaRequest),
            metaDataUpdateRequest,
            ownerSession
          )
        )

        result match {
          case Good(track) => Json.toJson(track) === Json.toJson(expectedResponse)
          case _ => true must beFalse
        }
      }
    }
    "When track coordinator fails" >> {
      "with Not Found" >> {
        trait NotFoundContext extends CreateTrackContext {
          stubOkidokiClient(users)
          stubTrackCoordinatorClient(
            trackAssetDataCreateRequest,
            metaDataUpdateRequest,
            Some(trackArtworkUpdateResult),
            NotFound().bad
          )
        }

        "Returns not found" in new NotFoundContext {
          val result = Await.result(
            trackUpdateService.createTrack(
              trackAsset = trackAssetDataCreateRequest,
              maybeAlbumArt = Some(trackArtworkMetaRequest),
              metaDataUpdateRequest,
              ownerSession
            )
          )

          result.isLeft
          result match {
            case Bad(NotFound(msg)) => msg === "Resource not found"
            case _ => true must beFalse
          }
        }
      }
      "with Invalid Request" >> {
        trait InvalidRequestContext extends CreateTrackContext {
          stubOkidokiClient(users)
          stubTrackCoordinatorClient(
            trackAssetDataCreateRequest,
            metaDataUpdateRequest,
            Some(trackArtworkUpdateResult),
            NotValid("invalid request").bad
          )
        }

        "Returns invalid request" in new InvalidRequestContext {
          val result = Await.result(
            trackUpdateService.createTrack(
              trackAsset = trackAssetDataCreateRequest,
              maybeAlbumArt = Some(trackArtworkMetaRequest),
              metaDataUpdateRequest,
              ownerSession
            )
          )

          result.isLeft
          result match {
            case Bad(NotValid(msg)) => msg === List("invalid request")
            case _ => true must beFalse
          }
        }
      }
    }

    "When user fetch fails" >> {
      trait FailureContext extends CreateTrackContext {
        stubOkidokiClient(List())

        stubTrackCoordinatorClient(
          trackAssetDataCreateRequest,
          metaDataUpdateRequest,
          Some(trackArtworkUpdateResult),
          Good(trackCoordinatorTrack)
        )
      }

      "Returns not found" in new FailureContext {
        val result = Await.result(
          trackUpdateService.createTrack(
            trackAsset = trackAssetDataCreateRequest,
            maybeAlbumArt = Some(trackArtworkMetaRequest),
            metaDataUpdateRequest,
            ownerSession
          )
        )

        result.isLeft
        result match {
          case Bad(NotFound(msg)) => msg === "Resource not found"
          case _ => true must beFalse
        }
      }
    }
  }
}
