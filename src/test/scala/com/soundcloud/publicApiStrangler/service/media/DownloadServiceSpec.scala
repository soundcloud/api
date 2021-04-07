package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.tracks.TracksClient
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito
import proto.soundcloud.tracks.api.{GetDownloadRequest, GetDownloadResponse, MediaService}

class DownloadServiceSpec extends UnitSpecification with Mockito {
  trait FetchContext extends Scope {
    val session = new UserSessionBuilder().setUser(Urn("users", "soundcloud", "1")).build
    val urn = Urn("soundcloud", "tracks", "2")
    val secretToken = Some("super secret")

    // subject and mocked dependencies
    val tracksClient = mock[TracksClient]
    val mediaService = mock[MediaService]
    val rolloutClient = mock[Rollout]

    val subject = new DownloadService(tracksClient, mediaService, rolloutClient)

    val downloadUrl = "https://soundcloud.com"

    val twirpDownloadRequest = GetDownloadRequest(
      userSession = Some(session.asProtoSession),
      urn = urn.toString,
      secretToken = secretToken,
      skipLogging = Some(false)
    )

    when(rolloutClient.isActive(any[RolloutFeature])).thenReturn(Future.True)
  }

  "fetchDownloadUrl using tracks client" >> {
    "Returns valid download Url from tracks service" in new FetchContext {
      when(mediaService.getDownload(twirpDownloadRequest))
        .thenReturn(Future.value(GetDownloadResponse(url = downloadUrl)))

      val response = Await.result(subject.download(session, urn, secretToken, skipLogging = false))
      response mustEqual DownloadOk(downloadUrl)
    }

    "Returns error when not found returned from tracks service" in new FetchContext {
      when(mediaService.getDownload(twirpDownloadRequest)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.NotFound, "download not found")
        )
      )

      val response = Await.result(subject.download(session, urn, secretToken, skipLogging = false))
      response mustEqual DownloadNotFound
    }

    "Returns error when not authorized returned from tracks service" in new FetchContext {
      when(mediaService.getDownload(twirpDownloadRequest)).thenReturn(
        Future.exception(
          TwinagleException(ErrorCode.Unauthenticated, "download not authorised")
        )
      )

      val response = Await.result(subject.download(session, urn, secretToken, skipLogging = false))
      response mustEqual DownloadNotFound
    }
  }
}
