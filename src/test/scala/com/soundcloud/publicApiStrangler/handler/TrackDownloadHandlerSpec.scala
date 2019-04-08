package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.service.media.{DownloadNotFound, DownloadOk, DownloadResponse, DownloadService, TrackAccessRecorderService}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future

class TrackDownloadHandlerSpec extends UnitSpecification {

  class FakeTrackAccessRecorderService extends TrackAccessRecorderService(null) {
    override def recordDownloadAccess(session: UserSession, request: HandlerRequest, trackUrn: Urn)(action: => Future[Response]): Future[Response] =
      action
  }

  trait Context extends HandlerSpecificationScope {
    val session = mock[UserSession]
    val userAuth = new FakeUserAuthentication(session)
    val mothershipDispatcher = mock[DispatchToMothershipHandler]
    val downloadService = mock[DownloadService]
    lazy val mediaServiceEnabled: Boolean = false

    lazy val handler = new TrackDownloadHandler(
      userAuth,
      mothershipDispatcher,
      new FakeTrackAccessRecorderService,
      downloadService,
      () => Future.value(mediaServiceEnabled))

    override def routingDefinitions = Routing.forTrackDownloadHandler(handler)
  }

  List(
    "/tracks/999/download",
    "/tracks/999/download/",
    "/tracks/999/download.json",
    "/tracks/999/download.json/"
  ).foreach { path =>
    "with mothership dispatcher" >> {
      trait MothershipContext extends Context {
        override lazy val mediaServiceEnabled = false
        val expectedResponse = ResponseBuilder.ok("foobar")
        mothershipDispatcher.dispatch(any[HandlerRequest]) returns Future.value(expectedResponse)
      }

      s"GET $path" in new MothershipContext {
        get(handler.handle, path) ==== expectedResponse
      }
    }
  }

  List(
    "/tracks/999/download",
    "/tracks/999/download/",
    "/tracks/999/download.json",
    "/tracks/999/download.json/"
  ).foreach { path =>
    "with media-service" >> {
      trait MediaServiceContext extends Context {
        override lazy val mediaServiceEnabled = true
        val downloadResponse: DownloadResponse
        downloadService.download(session, Urn("soundcloud", "tracks", "999"), None) returns Future.value(downloadResponse)
      }

      "when download is found" >> {
        trait FoundDownloadContext extends MediaServiceContext {
          override lazy val downloadResponse = DownloadOk("https://download-url")
        }

        s"GET $path should return 302" in new FoundDownloadContext {
          val response = get(handler.handle, path)
          response.status ==== Status.Found
          response.headerMap("Location") ==== "https://download-url"
        }
      }

      "when download is not found" >> {
        trait FoundDownloadContext extends MediaServiceContext {
          override lazy val downloadResponse = DownloadNotFound
        }

        s"GET $path should return 404" in new FoundDownloadContext {
          val response = get(handler.handle, path)
          response.status ==== Status.NotFound
        }
      }
    }
  }

  "with a secret token" >> {
    trait MediaServiceWithSecretTokenContext extends Context {
      override lazy val mediaServiceEnabled = true
      downloadService.download(session, Urn("soundcloud", "tracks", "999"), Some("itsasecret")) returns Future.value(DownloadOk("https://download-url"))
    }

    s"should return 302" in new MediaServiceWithSecretTokenContext {
      val response = get(handler.handle, "/tracks/999/download?secret_token=itsasecret")
      response.status ==== Status.Found
      response.headerMap("Location") ==== "https://download-url"
    }
  }
}
