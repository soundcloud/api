package com.soundcloud.publicApiStrangler.mapper.waveform

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.util.Url
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.media.{TrackWaveformUrl, WaveformUrlsRepository}
import com.soundcloud.publicApiStrangler.authorization.policies.ContentPolicy
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{verify, when}

class WaveformMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val waveformUrlsRepoMock = mock[WaveformUrlsRepository]
    val mapper = new WaveformMapper(waveformUrlsRepoMock)
    val session = new UserSessionBuilder().build()
    implicit val context = new MappingContext(mock[UserSession])
  }

  "empty inputs" >> {
    "returns empty Map" in new Context {
      Await.result(mapper.map(session, Set.empty)).isEmpty must beTrue
    }
  }

  "only one input" >> {

    trait OneContext extends Context {
      val uid = "8779as"
      val policy = ContentPolicy.ALLOW
      val waveformUrl = TrackWaveformUrl(uid, Url("jsonUrl"), Url("pngUrl"), "preview")

      val waveformRequest = WaveformRequestParams(uid, policy)

      when(waveformUrlsRepoMock.fetchWaveformUrlsToMap(===(session), ===(Map(uid -> policy))))
        .thenReturn(Future(Map(uid -> waveformUrl)))
    }


    "returns the mapping of given UID" in new OneContext {
      val actual = Await.result(mapper.map(session, Set(waveformRequest)))
      actual must haveSize(1)
      actual.keys must contain(waveformRequest)
      actual(waveformRequest).resource ==== waveformUrl

      verify(waveformUrlsRepoMock).fetchWaveformUrlsToMap(session, Map(uid -> policy))
    }
  }

  "multiple inputs" >> {

    trait OneContext extends Context {
      val uid = "8779as"
      val policy = ContentPolicy.ALLOW
      val waveformUrl = TrackWaveformUrl(uid, Url("jsonUrl"), Url("pngUrl"), "preview")
      val waveformRequest = WaveformRequestParams(uid, policy)

      val uid2 = "2222asdasd"
      val policy2 = ContentPolicy.ALLOW
      val waveformUrl2 = TrackWaveformUrl(uid2, Url("jsonUrl2"), Url("pngUrl2"), "preview")
      val waveformRequest2 = WaveformRequestParams(uid2, policy2)

      when(waveformUrlsRepoMock.fetchWaveformUrlsToMap(session, Map(uid -> policy, uid2 -> policy2)))
        .thenReturn(Future(Map(uid -> waveformUrl, uid2 -> waveformUrl2)))
    }


    "returns the mapping of given UID" in new OneContext {
      val actual = Await.result(mapper.map(session, Set(waveformRequest, waveformRequest2)))
      actual must haveSize(2)
      actual.keys must contain(waveformRequest)
      actual.keys must contain(waveformRequest2)
      actual(waveformRequest).resource ==== waveformUrl
      actual(waveformRequest2).resource ==== waveformUrl2

      verify(waveformUrlsRepoMock).fetchWaveformUrlsToMap(session, Map(uid -> policy, uid2 -> policy2))
    }
  }
}
