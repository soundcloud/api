package com.soundcloud.publicApiStrangler.service

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, TracksClient, VisibleTrackBuilder}
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime

class TrackVisibilityServiceSpec extends Specification with Mockito {

  trait Context extends Scope {
    val session = (new UserSessionBuilder).setUser(Urn("soundcloud", "users", "123")).build()
    val trackUrn = Urn("soundcloud", "tracks", "432")
    val tracksClient = mock[TracksClient]
    val service = new TrackVisibilityService(tracksClient)
    val trackRequest = TrackRequest(trackUrn, None)
    val transcodings = List(
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    )
    lazy val visibleTrack =
      (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(None).setTranscodings(transcodings).build

    tracksClient.visibleTracks(session, List(trackRequest)) returns Future.value(List(visibleTrack))
  }

  "#visibleTracks" >> {
    "returns visible tracks" in new Context {
      Await.result(service.tracks(session, List(trackRequest))) ==== List(visibleTrack)
    }

    "disabled track" >> {
      trait DisabledTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(Some(LocalDateTime.now())).build
      }

      "filters out disabled tracks" in new DisabledTrackContext {
        Await.result(service.tracks(session, List(trackRequest))) ==== List.empty
      }
    }

    "transcoding filter track" >> {
      trait TranscodingFilterTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setTranscodings(List.empty).build
      }

      "filters out non 'audio/mpeg' tracks" in new TranscodingFilterTrackContext {
        Await.result(service.tracks(session, List(trackRequest))) ==== List.empty
      }
    }
  }
}
