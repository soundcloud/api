package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.twitter.util.{Await, Future}
import com.soundcloud.jvmkit.module.util.{Path, Url, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf
import com.twitter.finagle.http.Status

class WaveformUrlsRepositorySpec extends UnitSpecification {

  "WaveformUrlsRepository" should {
    trait WaveformContext extends Scope {
      val userSession = (new UserSessionBuilder).build()
      val mediaServiceClient = mock[MediaServiceClient]
      val moshimoshi = mock[JsonClient]
      val waveformUrlsRepository = new WaveformUrlsRepository(moshimoshi, mediaServiceClient)
      val desiredTrack1Uid = "2bAA4VpdwqqY"
      val desiredTrack1Urn = Urn("soundcloud:tracks:11112")
      val desiredTrack2Uid = "3bAA4VpdwqqY"
      val desiredTrack2Urn = Urn("soundcloud:tracks:11113")
      val track1url1 = TrackWaveformUrl(desiredTrack1Uid, Url("http://track1/json"), Url("http://track1/png"), "stream")
      val track1url2 = TrackWaveformUrl(desiredTrack1Uid, Url("http://track1/preview/json"), Url("http://track1/preview/png"), "preview", Some(90000))
      val track2url1 = TrackWaveformUrl(desiredTrack2Uid, Url("http://track2/json"), Url("http://track2/png"), "stream")
      val track2url2 = TrackWaveformUrl(desiredTrack2Uid, Url("http://track2/preview/json"), Url("http://track2/preview/png"), "preview", Some(30000))
      mediaServiceClient.trackWaveformUrlsFor(userSession, List(desiredTrack1Uid, desiredTrack2Uid)) returns Future.value(Set(track1url1, track1url2, track2url1, track2url2))
    }

    "when we don't have to fetch uid through moshimoshi" >> {

      "returns all non preview urls in case content policy = allow" in new WaveformContext {
        val track1ContentPolicy = ContentPolicy.ALLOW
        val track2ContentPolicy = ContentPolicy.ALLOW
        val map = Map(desiredTrack1Uid -> track1ContentPolicy, desiredTrack2Uid -> track2ContentPolicy)
        val tracks = Await.result(waveformUrlsRepository.fetchWaveformUrls(userSession, map))
        tracks ==== Set(track1url1, track2url1)
        there was noCallsTo(moshimoshi)
      }

      "return all non preview waveforms but urls replaced with preview urls in case content policy = snip" in new WaveformContext {
        val track1ContentPolicy = ContentPolicy.SNIP
        val track2ContentPolicy = ContentPolicy.ALLOW
        val map = Map(desiredTrack1Uid -> track1ContentPolicy, desiredTrack2Uid -> track2ContentPolicy)
        val tracks = Await.result(waveformUrlsRepository.fetchWaveformUrls(userSession, map))
        val adaptedUrl = TrackWaveformUrl(desiredTrack1Uid, Url("http://track1/preview/json"), Url("http://track1/preview/png"), "stream", Some(90000))
        tracks ==== Set(adaptedUrl, track2url1)
        there was noCallsTo(moshimoshi)
      }

      "return all non preview waveforms in a Map but urls replaced with preview urls in case content policy = snip" in new WaveformContext {
        val track1ContentPolicy = ContentPolicy.SNIP
        val track2ContentPolicy = ContentPolicy.ALLOW
        val map = Map(desiredTrack1Uid -> track1ContentPolicy, desiredTrack2Uid -> track2ContentPolicy)
        val tracks = Await.result(waveformUrlsRepository.fetchWaveformUrlsToMap(userSession, map))
        val adaptedUrl = TrackWaveformUrl(desiredTrack1Uid, Url("http://track1/preview/json"), Url("http://track1/preview/png"), "stream", Some(90000))
        tracks.size ==== 2
        tracks(desiredTrack1Uid) ==== adaptedUrl
        tracks(desiredTrack2Uid) ==== track2url1
        there was noCallsTo(moshimoshi)
      }
    }

    trait WaveformContextFetchUid extends WaveformContext {
      val desiredTrackJson = withContentsOf("moshimoshi", "track_11112_11113")
      moshimoshi.getWithSession(userSession, Path("/tracks") / "fetch", Set(desiredTrack1Urn, desiredTrack2Urn), Headers.empty) returns Future.value(jsonResponse(Status.Ok, desiredTrackJson))
    }

    "when we have to fetch uids through moshimoshi" >> {

      "returns all non preview urls in case content policy = allow" in new WaveformContextFetchUid {
        val track1Auth = new ContentAuthorization(desiredTrack1Urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
        val track2Auth = new ContentAuthorization(desiredTrack2Urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
        val waveforms = Await.result(waveformUrlsRepository.fetchWaveformUrls(userSession, Set(track1Auth, track2Auth)))
        waveforms.size ==== 2
        waveforms(desiredTrack1Urn) ==== track1url1
        waveforms(desiredTrack2Urn) ==== track2url1
        there was one(moshimoshi).getWithSession(userSession, Path("/tracks") / "fetch", Set(desiredTrack1Urn, desiredTrack2Urn), Headers.empty)
      }

      "return all non preview waveforms but urls replaced with preview urls in case content policy = snip" in new WaveformContextFetchUid {
        val track1Auth = new ContentAuthorization(desiredTrack1Urn, ContentPolicy.SNIP, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
        val track2Auth = new ContentAuthorization(desiredTrack2Urn, ContentPolicy.ALLOW, Reason.UNKNOWN, MonetizationModel.NOT_APPLICABLE)
        val waveforms = Await.result(waveformUrlsRepository.fetchWaveformUrls(userSession, Set(track1Auth, track2Auth)))
        val adaptedUrl = TrackWaveformUrl(desiredTrack1Uid, Url("http://track1/preview/json"), Url("http://track1/preview/png"), "stream", Some(90000))
        waveforms.size ==== 2
        waveforms(desiredTrack1Urn) ==== adaptedUrl
        waveforms(desiredTrack2Urn) ==== track2url1
        there was one(moshimoshi).getWithSession(userSession, Path("/tracks") / "fetch", Set(desiredTrack1Urn, desiredTrack2Urn), Headers.empty)
      }

    }
  }

}
