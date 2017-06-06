package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, ListParam, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json


class MediaServiceClientSpec extends UnitSpecification {

  "MediaServiceClient" should {

    "Stream urls" >> {

      trait Context extends Scope {
        val jsonService = mock[JsonClient]
        val userSession = mock[UserSession]
        val urn = Urn("soundcloud", "tracks", "133")
        val json = Json.obj()
        val requestParams = Params(
          "uid" -> "uiduid",
          "user_id" -> "3157",
          "duration" -> "1547",
          "ssl" -> "false",
          "content_policy" -> "ALLOW",
          "content_restrictions" -> ListParam(List[String]())
        )
        val allExpectedRequestParams = requestParams ++ Params("method" -> "GET", "legacy" -> "false")
        val trackStreamUrlMapper = mock[TrackStreamUrlMapper]
        val trackWaveformUrlMapper = mock[TrackWaveformUrlMapper]
        val mediaServiceClient = new MediaServiceClient(jsonService, trackStreamUrlMapper, trackWaveformUrlMapper)
        val mediaUrl1 = mock[MediaUrl]
        val mediaUrl2 = mock[MediaUrl]
        val contentAuthorization = mock[ContentAuthorization]
        contentAuthorization.getPolicy returns ContentPolicy.ALLOW
        contentAuthorization.getContentRestrictions returns Set()
      }

      "return MediaUrls for valid input and http response" in new Context {
        jsonService.getWithSession(userSession, Path("/media") / urn / "streams", allExpectedRequestParams, Headers.empty) returns Future.value(jsonResponse(Status.Ok, json))
        trackStreamUrlMapper.map(json) returns Set(mediaUrl1, mediaUrl2)
        val trackStreams = Await.result(mediaServiceClient.trackStreamUrlsFor(userSession, urn, requestParams, contentAuthorization))
        trackStreams ==== Set(mediaUrl1, mediaUrl2)
      }

      "return empty seq in case media service response is 'Not Found'" in new Context {
        jsonService.getWithSession(userSession, Path("/media") / urn / "streams", allExpectedRequestParams, Headers.empty) returns Future.value(jsonResponse(Status.NotFound, json))
        val trackStreamUrls = Await.result(mediaServiceClient.trackStreamUrlsFor(userSession, urn, requestParams, contentAuthorization))
        trackStreamUrls ==== Set()
        there was no(trackStreamUrlMapper).map(json)
      }

      "return empty seq in case policy = BLOCK, do not make a request to the downstream" in new Context {
        contentAuthorization.getPolicy returns ContentPolicy.BLOCK

        val trackStreamUrls = Await.result(mediaServiceClient.trackStreamUrlsFor(userSession, urn, requestParams, contentAuthorization))
        trackStreamUrls ==== Set()
        there was no(jsonService).getWithSession(userSession, Path("/media") / urn / "streams", allExpectedRequestParams, Headers.empty)
      }
    }

    "Waveform urls" >> {

      trait WaveformContext extends Scope {
        val jsonService = mock[JsonClient]
        val userSession = mock[UserSession]
        val trackUid1 = "bdfsp"
        val trackUid2 = "qagpd"
        val json = Json.obj()
        val otherJson = Json.obj("a" -> "b")
        val trackStreamUrlMapper = mock[TrackStreamUrlMapper]
        val trackWaveformUrlMapper = mock[TrackWaveformUrlMapper]
        val mediaServiceClient = new MediaServiceClient(jsonService, trackStreamUrlMapper, trackWaveformUrlMapper)
        val waveFormUrl1 = mock[TrackWaveformUrl]
        val waveFormUrl2 = mock[TrackWaveformUrl]
      }

      "return empty set when no ids given as input and don't call jsonService" in new WaveformContext {
        val waveformUrls = Await.result(mediaServiceClient.trackWaveformUrlsFor(userSession, List()))
        waveformUrls.isEmpty ==== true
        there was noCallsTo(jsonService)
      }

      "return TrackWaveformUrls for valid input and http response" in new WaveformContext {
        jsonService.getWithSession(userSession, Path("/waveforms"), Params("uid" -> ListParam(List(trackUid1, trackUid2))), Headers.empty) returns Future.value(jsonResponse(Status.Ok, json))
        trackWaveformUrlMapper.map(json) returns Set(waveFormUrl1, waveFormUrl2)
        val waveformUrls = Await.result(mediaServiceClient.trackWaveformUrlsFor(userSession, List(trackUid1, trackUid2)))
        waveformUrls ==== Set(waveFormUrl1, waveFormUrl2)
      }

      "should send several batches of requests when number of uids is larger than 50" in new WaveformContext {
        val allTrackUids = Range.inclusive(1, 52).map(value => value.toString).toList
        val first50Trackuids = allTrackUids.slice(0, 50)
        val last2Trackuids = allTrackUids.slice(50, 52)
        val first50Urls = first50Trackuids.map(url => mock[TrackWaveformUrl])
        val last2Urls = last2Trackuids.map(url => mock[TrackWaveformUrl])

        jsonService.getWithSession(userSession, Path("/waveforms"), Params("uid" -> ListParam(first50Trackuids)), Headers.empty) returns Future.value(jsonResponse(Status.Ok, json))
        jsonService.getWithSession(userSession, Path("/waveforms"), Params("uid" -> ListParam(last2Trackuids)), Headers.empty) returns Future.value(jsonResponse(Status.Ok, otherJson))
        trackWaveformUrlMapper.map(json) returns first50Urls.toSet
        trackWaveformUrlMapper.map(otherJson) returns last2Urls.toSet

        val waveformUrls = Await.result(mediaServiceClient.trackWaveformUrlsFor(userSession, allTrackUids))
        waveformUrls ==== (first50Urls ++ last2Urls).toSet

        there was one(trackWaveformUrlMapper).map(json)
        there was one(trackWaveformUrlMapper).map(otherJson)
        there was one(jsonService).getWithSession(userSession, Path("/waveforms"), Params("uid" -> ListParam(first50Trackuids)), Headers.empty)
        there was one(jsonService).getWithSession(userSession, Path("/waveforms"), Params("uid" -> ListParam(last2Trackuids)), Headers.empty)
      }
    }
  }
}
