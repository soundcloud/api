package com.soundcloud.publicApiStrangler.service.media

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.tracks.{
  DownloadErrorResponse,
  DownloadRequest,
  DownloadUrlResponse,
  TracksClient
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import org.specs2.mock.Mockito

class DownloadServiceSpec extends UnitSpecification with Mockito {
  trait FetchContext extends Scope {
    val session = new UserSessionBuilder().setUser(Urn("users", "soundcloud", "1")).build
    val urn = Urn("soundcloud", "tracks", "2")
    val secretToken = Some("super secret")

    // subject and mocked dependencies
    val tracksClient = mock[TracksClient]
    val subject = new DownloadService(tracksClient)

    val downloadUrl = "https://soundcloud.com"
    val downloadRequest = new DownloadRequest(urn, secretToken)
  }

  "fetchDownloadUrl using tracks client" >> {
    "Returns valid download Url from tracks service" in new FetchContext {
      when(tracksClient.downloadUrl(session, downloadRequest))
        .thenReturn(Future.value(DownloadUrlResponse(downloadUrl)))

      val response = Await.result(subject.download(session, urn, secretToken))
      response mustEqual (DownloadOk(downloadUrl))
    }

    "Returns error when not found returned from tracks service" in new FetchContext {
      when(tracksClient.downloadUrl(session, downloadRequest)).thenReturn(Future.value(DownloadErrorResponse))

      val response = Await.result(subject.download(session, urn, secretToken))
      response mustEqual (DownloadNotFound)
    }

    "Returns error when not authorized returned from tracks service" in new FetchContext {
      when(tracksClient.downloadUrl(session, downloadRequest)).thenReturn(Future.value(DownloadErrorResponse))

      val response = Await.result(subject.download(session, urn, secretToken))
      response mustEqual (DownloadNotFound)
    }
  }
}
