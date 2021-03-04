package com.soundcloud.publicApiStrangler.handler

import java.nio.file.{Files, Paths}
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.authorization.Track
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserMapper
import com.soundcloud.publicApiStrangler.client.trackcoordinator.{TrackCoordinatorClient, TrackCoordinatorTrack}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.{
  TrackArtworkUpdateRequest,
  TrackAssetDataCreateRequest,
  TrackAssetDataUpdateRequest,
  TrackMetadataCreateRequest,
  TrackMetadataUpdateRequest
}
import com.soundcloud.publicApiStrangler.service.CreatedTrack.CreatedTrack
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{
  TrackRepresentation,
  TrackRepresentationSpecContext,
  TrackUpdateService
}
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{FileElement, Status}
import com.twitter.io.Buf
import com.twitter.util.Future
import org.mockito.Mockito._
import play.api.libs.json._

class TracksHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {
  trait Context extends HandlerSpecificationScope {
    val trackCoordinator = mock[TrackCoordinatorClient]
    val trackUpdateService = mock[TrackUpdateService]
    val trackUrn = Urn("soundcloud", "tracks", "999")
    val userUrn = Urn("soundcloud", "users", "102661606")
    val loggedInUserUrn = Urn("soundcloud", "users", "2")
    val user = users.head
    val track = mock[Track]
    val mockTrackRepresentation = createTrackRepresentation()

    val emptyTrackUpdate = TrackMetadataUpdateRequest.fromForm(Map.empty).getOrElse(null)

    lazy val geo = new Geo("US")
    lazy val session = new UserSessionBuilder()
      .setUser(loggedInUserUrn)
      .setAgent(Urn("soundcloud", "applications", "v2"))
      .setGeo(geo)
      .build()

    lazy val handler =
      new TracksHandler(
        new FakeUserAuthentication(session),
        trackCoordinator,
        trackUpdateService
      )

    override def routingDefinitions = Routing.forTracksHandler(handler)

    trackCoordinator.deleteTrack(session, trackUrn) returns Future(Good(()))

    def setupMockForTrackUpdateMetadata(
        metadataUpdateOutcome: Outcome[TrackRepresentation],
        trackUpdate: TrackMetadataUpdateRequest,
        artworkUpdate: Option[TrackArtworkUpdateRequest] = None,
        assetUpdate: Option[TrackAssetDataUpdateRequest] = None
    ) = {

      when(
        trackUpdateService
          .updateTrack(
            artworkUpdate,
            assetUpdate,
            trackUpdate,
            mockTrackRepresentation.visibleTrack.urn,
            session
          )
      ).thenReturn(
        Future.value(metadataUpdateOutcome)
      )
    }
  }

  "PUT /tracks/:id" >> {
    "Json request" >> {
      trait SuccessContext extends Context {
        val requestBody =
          """
            | {
            |   "track": {
            |     "title": "changed",
            |     "description": "changed"
            |    }
            | }
            |""".stripMargin
        val trackUpdate = Json.parse(requestBody).as[TrackMetadataUpdateRequest]

        val expectedResponse = mockTrackRepresentation.copy(
          visibleTrack = mockTrackRepresentation.visibleTrack.copy(
            description = Some("changed"),
            title = "changed"
          )
        )
      }

      trait FailureContext extends Context {
        val requestBody =
          """
            | {
            |   "track": {
            |     "title": "changed",
            |     "description": "changed"
            |    }
            | }
            |""".stripMargin
        val trackUpdate = Json.parse(requestBody).as[TrackMetadataUpdateRequest]

        val invalidRequestBody =
          """
            | {
            |   "blabla": "i'm not a track update"
            | }
            |""".stripMargin
      }

      "returns 200 on successful update" in new SuccessContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = Good(expectedResponse), trackUpdate = trackUpdate)

        val response = put(path, body = requestBody)
        response.statusCode === 200
        response.contentString === Json.stringify(Json.toJson((expectedResponse)))
      }

      "returns 404 when track does not exist" in new FailureContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"
        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = NotFound().bad, trackUpdate = trackUpdate)

        val response = put(path, body = requestBody)
        response.statusCode === 404
      }

      "returns 500 on invalid request" in new FailureContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        val response = put(path, body = invalidRequestBody)
        response.statusCode === 400
      }
    }

    "application/x-www-form-urlencoded request" >> {
      trait SuccessContext extends Context {
        val requestBody = Seq[(String, String)](("track[title]", "changed"), ("track[description]", "changed"))
        val parsedRequestBody = Map[String, String]("title" -> "changed", "description" -> "changed")

        val trackUpdate = TrackMetadataUpdateRequest.fromForm(parsedRequestBody).getOrElse(null)
        val expectedResponse = mockTrackRepresentation.copy(
          visibleTrack = mockTrackRepresentation.visibleTrack.copy(
            description = Some("changed"),
            title = "changed"
          )
        )
      }

      trait FailureContext extends Context {
        val requestBody = Seq[(String, String)](("track[title]", "changed"), ("track[description]", "changed"))
        val parsedRequestBody = Map[String, String]("title" -> "changed", "description" -> "changed")

        val trackUpdate = TrackMetadataUpdateRequest.fromForm(parsedRequestBody).getOrElse(null)
      }

      "Returns a 200 on a valid request" in new SuccessContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = Good(expectedResponse), trackUpdate = trackUpdate)

        val response = putForm(path, body = requestBody)

        response.statusCode === 200
        response.contentString === Json.stringify(Json.toJson((expectedResponse)))
      }

      "returns a 404 if track does not exist" in new FailureContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = NotFound().bad, trackUpdate = trackUpdate)

        val response = putForm(path, body = requestBody)
        response.statusCode === 404
      }
    }

    "Multipart/form request" >> {
      trait SuccessContext extends Context {
        val requestBody = Seq[(String, String)](("track[title]", "changed"), ("track[description]", "changed"))
        val parsedRequestBody = Map[String, String]("title" -> "changed", "description" -> "changed")

        val trackUpdate = TrackMetadataUpdateRequest.fromForm(parsedRequestBody).getOrElse(null)
        val expectedResponse = mockTrackRepresentation.copy(
          visibleTrack = mockTrackRepresentation.visibleTrack.copy(
            description = Some("changed"),
            title = "changed"
          )
        )
      }

      trait FailureContext extends Context {
        val requestBody = Seq[(String, String)](("track[title]", "changed"), ("track[description]", "changed"))
        val parsedRequestBody = Map[String, String]("title" -> "changed", "description" -> "changed")

        val trackUpdate = TrackMetadataUpdateRequest.fromForm(parsedRequestBody).getOrElse(null)
      }

      "returns a 200 on a valid request" in new SuccessContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = Good(expectedResponse), trackUpdate = trackUpdate)

        val response = putForm(path, body = requestBody, isMultipart = true)
        response.statusCode === 200
        response.contentString === Json.stringify(Json.toJson((expectedResponse)))
      }

      "returns a 404 if track when track does not exist" in new FailureContext {
        val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

        setupMockForTrackUpdateMetadata(metadataUpdateOutcome = NotFound().bad, trackUpdate = trackUpdate)

        val response = putForm(path, body = requestBody, isMultipart = true)
        response.statusCode === 404
      }

      "file upload" >> {
        trait WithArtworkData {
          val bytes = Files.readAllBytes(
            Paths.get(this.getClass.getClassLoader.getResource("test-image.jpg").toURI)
          )

          val file =
            FileElement("track[artwork_data]", Buf.ByteArray.Owned(bytes), Some("image/jpeg"))
        }

        "can upload artwork" in new SuccessContext with WithArtworkData {
          val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

          when(
            trackUpdateService
              .updateTrack(
                anyObject[Option[TrackArtworkUpdateRequest]],
                ===(None),
                ===(emptyTrackUpdate),
                ===(mockTrackRepresentation.visibleTrack.urn),
                ===(session)
              )
          ).thenReturn(
            Future.value(Good(mockTrackRepresentation))
          )

          val response = putForm(
            path,
            maybeFile = Some(file),
            isMultipart = true
          )

          response.statusCode === 200
          response.contentString === Json.stringify(Json.toJson(mockTrackRepresentation))

        }
      }

      "asset data upload" >> {
        trait WithAssetData extends Context {
          val requestBody = Seq[(String, String)](("track[uid]", "12345"), ("track[original_filename]", "audio.mp3"))
          val parsedRequestBody = Map[String, String]("uid" -> "12345", "original_filename" -> "audio.mp3")

          val assetUpdate = TrackAssetDataUpdateRequest.fromForm(parsedRequestBody)
          val trackUpdate = TrackMetadataUpdateRequest.fromForm(parsedRequestBody).getOrElse(null)
          val expectedResponse = mockTrackRepresentation
        }

        "can upload track asset data" in new WithAssetData {
          val path = s"/tracks/${mockTrackRepresentation.visibleTrack.urn.identifier}"

          setupMockForTrackUpdateMetadata(
            assetUpdate = assetUpdate,
            trackUpdate = trackUpdate,
            metadataUpdateOutcome = Good(expectedResponse)
          )

          val response = putForm(path, body = requestBody, isMultipart = true)
          response.statusCode === 200
          response.contentString === Json.stringify(Json.toJson((expectedResponse)))
        }
      }
    }
  }

  "POST /tracks" >> {

    trait PostContext extends Context {
      val users = Fixtures.okidokiUsers.as[List[JsObject]].map(UserMapper(_))
      val userObj = users.head
    }

    "application/x-www-form-urlencoded request" >> {

      trait UrlEncodedContext extends PostContext {
        val path = "/tracks"
        def stubTrackUpdateServiceCreate(
            createdTrackOutcome: Outcome[CreatedTrack],
            trackUpdate: Outcome[TrackMetadataCreateRequest],
            artworkUpdate: Option[TrackArtworkUpdateRequest] = None,
            assetUpdate: TrackAssetDataCreateRequest
        ) = {
          trackUpdate.map(metadata => {
            when(
              trackUpdateService
                .createTrack(
                  assetUpdate,
                  artworkUpdate,
                  metadata,
                  session
                )
            ).thenReturn(
              Future.value(createdTrackOutcome)
            )
          })
        }

        val requestBody = Seq[(String, String)](
          ("track[uid]", "12345"),
          ("track[original_filename]", "audio.mp3"),
          ("track[title]", "my track")
        )
        val parsedRequestBody =
          Map[String, String]("uid" -> "12345", "original_filename" -> "audio.mp3", "title" -> "my track")
        val assetUpdate = TrackAssetDataCreateRequest("audio.mp3", "12345")
        val trackUpdate = TrackMetadataCreateRequest.fromForm(parsedRequestBody)
        val bytes = Files.readAllBytes(
          Paths.get(this.getClass.getClassLoader.getResource("test-image.jpg").toURI)
        )
        val artworkUpdate = Some(TrackArtworkUpdateRequest(bytes))
        val file =
          FileElement("track[artwork_data]", Buf.ByteArray.Owned(bytes), Some("image/jpeg"), Some("test-image.jpg"))
      }

      trait SuccessContext extends UrlEncodedContext {
        val trackCoordinatorTrack = Fixtures.trackCoordinatorTrack.as[TrackCoordinatorTrack]

        val expectedResponse = CreatedTrack(trackCoordinatorTrack, userObj, None)
      }

      trait NotFoundContext extends UrlEncodedContext {
        val expectedResponse = NotFound().bad
      }

      trait InvalidRequestContext extends UrlEncodedContext {
        val expectedResponse = NotValid("invalid request").bad
      }

      "Returns a 201 on a valid request" in new SuccessContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = Good(expectedResponse),
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody)

        response.statusCode === 201
        response.headerMap.get("Location") === Some("https://api.soundcloud.com/tracks/174088262")
        response.contentString === Json.stringify(Json.toJson((expectedResponse)))
      }

      "returns a 404 if track does not exist" in new NotFoundContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = expectedResponse,
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody)
        response.statusCode === 404
      }

      "returns a 400 for an invalid request" in new InvalidRequestContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = expectedResponse,
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody)
        response.statusCode === 400
      }

      "Generates unprocessable entity response if no asset found" in new NotFoundContext {
        override val requestBody = Seq[(String, String)](("track[title]", "my track"))
        val response = postForm(path, body = requestBody)
        response.statusCode === 422
      }

      "Generates unprocessable entity if no title found" in new NotFoundContext {
        override val requestBody = Seq[(String, String)](("track[uid]", "12345"), ("original_filename" -> "audio.mp3"))
        val response = postForm(path, body = requestBody)
        response.statusCode === 422
      }
    }

    "Multipart/form request" >> {

      trait MultiPartFormContext extends PostContext {
        val path = "/tracks"
        def stubTrackUpdateServiceCreate(
            createdTrackOutcome: Outcome[CreatedTrack],
            trackUpdate: Outcome[TrackMetadataCreateRequest],
            assetUpdate: TrackAssetDataCreateRequest
        ) = {
          trackUpdate.map(metadata => {
            when(
              trackUpdateService
                .createTrack(
                  ===(assetUpdate),
                  anyObject[Option[TrackArtworkUpdateRequest]],
                  ===(metadata),
                  ===(session)
                )
            ).thenReturn(
              Future.value(createdTrackOutcome)
            )
          })
        }
        val requestBody = Seq[(String, String)](
          ("track[uid]", "12345"),
          ("track[original_filename]", "audio.mp3"),
          ("track[title]", "my track")
        )
        val parsedRequestBody =
          Map[String, String]("uid" -> "12345", "original_filename" -> "audio.mp3", "title" -> "my track")
        val assetUpdate = TrackAssetDataCreateRequest("audio.mp3", "12345")
        val trackUpdate = TrackMetadataCreateRequest.fromForm(parsedRequestBody)
        val bytes = Files.readAllBytes(
          Paths.get(this.getClass.getClassLoader.getResource("test-image.jpg").toURI)
        )
        val artworkUpdate = Some(TrackArtworkUpdateRequest(bytes))
        val file =
          FileElement("track[artwork_data]", Buf.ByteArray.Owned(bytes), Some("image/jpeg"))
      }

      trait SuccessContext extends MultiPartFormContext {
        val trackCoordinatorTrack = Fixtures.trackCoordinatorTrack.as[TrackCoordinatorTrack]
        val expectedResponse = CreatedTrack(trackCoordinatorTrack, userObj, None)
      }

      trait FailureContext extends MultiPartFormContext {
        val expectedResponse = NotFound().bad
      }

      trait InvalidRequestContext extends MultiPartFormContext {
        val expectedResponse = NotValid("invalid request").bad
      }

      "Returns a 201 on a valid request" in new SuccessContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = Good(expectedResponse),
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody, maybeFile = Some(file), isMultipart = true)

        response.statusCode === 201
        response.headerMap.get("Location") === Some("https://api.soundcloud.com/tracks/174088262")
        response.contentString === Json.stringify(Json.toJson((expectedResponse)))
      }

      "returns a 404 if track does not exist" in new FailureContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = expectedResponse,
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody, maybeFile = Some(file), isMultipart = true)
        response.statusCode === 404
      }

      "returns a 400 for an invalid request" in new InvalidRequestContext {
        stubTrackUpdateServiceCreate(
          createdTrackOutcome = expectedResponse,
          trackUpdate = trackUpdate,
          assetUpdate = assetUpdate
        )

        val response = postForm(path, body = requestBody)
        response.statusCode === 400
      }

      "Generates unprocessable entity response if no asset found" in new FailureContext {
        override val requestBody = Seq[(String, String)](("track[title]", "my track"))
        val response = postForm(path, body = requestBody)
        response.statusCode === 422
      }
      "Generates unprocessable entity if no title found" in new FailureContext {
        override val requestBody = Seq[(String, String)](("track[uid]", "12345"), ("original_filename" -> "audio.mp3"))
        val response = postForm(path, body = requestBody)
        response.statusCode === 422
      }
    }
  }

  "DELETE /tracks/:id" >> {
    "succeeds" in new Context {
      val response = delete("/tracks/999")
      response.status ==== Status.Ok
    }

    "not found" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotFound().bad)

      val response = delete("/tracks/999")
      response.status ==== Status.NotFound
    }

    "handles server errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotValid("").bad)

      val response = delete("/tracks/999")
      response.status ==== Status.InternalServerError
    }

    "handles client errors from Track Coordinator" in new Context {
      trackCoordinator.deleteTrack(session, trackUrn) returns Future(NotValid("").bad)

      val response = delete("/tracks/999")
      response.status ==== Status.InternalServerError
    }
  }
}
