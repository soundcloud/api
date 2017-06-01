package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.ContentAuthorization
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.joda.time.DateTime

class MediaUrlsRepositorySpec extends UnitSpecification {


  "MediaUrlsRepository" should {

    trait StreamContext extends Scope {
      val userSession = (new UserSessionBuilder).build()
      val mediaServiceClient = mock[MediaServiceClient]
      val mediaUrlsRepository = new MediaUrlsRepository(mediaServiceClient)
      val desiredTrack = new Urn("soundcloud:tracks:11112")
      val mediaServiceParamsSsl = Params("ssl" -> "true")
      val expiresAt = DateTime.now()

      val httpUrl = Url("http://track.mp3")
      val hlsUrl = Url("http://hlstrack.mp3")
      val rtmpUrl = Url("http://rtmptrack.mp3")

      val httpMediaUrl = MediaUrl("http_mp3_128_url", httpUrl, expiresAt)
      val hlsMediaUrl = MediaUrl("hls_mp3_128_url", hlsUrl, expiresAt)
      val rtmpMediaUrl = MediaUrl("rtmp_mp3_128_url", rtmpUrl, expiresAt)

      val regularTrackStreams = Set(httpMediaUrl, hlsMediaUrl, rtmpMediaUrl)

      val contentAuthorization = mock[ContentAuthorization]
      mediaServiceClient.trackStreamUrlsFor(userSession, desiredTrack, mediaServiceParamsSsl, contentAuthorization) returns Future.value(regularTrackStreams)
    }

    "return regular streams when policy = allow" in new StreamContext {
      val mediaUrls = Await.result(mediaUrlsRepository.byUrn(userSession, desiredTrack, contentAuthorization))
      mediaUrls ==== regularTrackStreams
    }
  }

}
