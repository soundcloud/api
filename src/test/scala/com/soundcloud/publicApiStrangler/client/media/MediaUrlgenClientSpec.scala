package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, ListParam, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class MediaUrlgenClientSpec extends UnitSpecification {

  "MediaUrlgenClient" should {

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
        val mediaServiceClient = new MediaUrlgenClient(jsonService, trackStreamUrlMapper)
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
  }
}
