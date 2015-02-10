package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.finagle.{ResponseBuilder, Request}
import com.soundcloud.bff.media.{MediaUrl, MediaUrlsRepository}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.twitter.util.{Future, Await}



class TrackStreamSnipHandlerSpec extends UnitSpecification {

  "TrackStreamSnipHandler" should  {

    trait Context extends Scope {
      val mothershipDispatcher = mock[DispatchToMothershipHandler]
      val contentAuthService = mock[ContentAuthorizationService]
      val mediaUrlsRepository = mock[MediaUrlsRepository]

      val handler = new TrackStreamSnipHandler(mothershipDispatcher, contentAuthService, mediaUrlsRepository)

      val trackId = "334030"
      val request = mock[Request]
      val paramMap = scala.collection.mutable.Map("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)

      val userSession = mock[UserSession]
      val mapper = mock[TrackStreamResponseMapper]
      val contentAuth = mock[ContentAuthorization]

      contentAuthService.findRulesApplicableTo(userSession, Seq(trackUrn)) returns
        Future.value(Seq(contentAuth))

      def responseBuilder(statusCode:Int) =
        Future.value(new ResponseBuilder().status(statusCode))

    }

    "should return pubapi response in case pubapi returns client error" in new Context {

      val clientErrorResponse = responseBuilder(401)
      mothershipDispatcher.dispatch(request) returns clientErrorResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(clientErrorResponse) ==== responseBuilder
      there was one(contentAuthService).findRulesApplicableTo(userSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }

    "should return pubapi response in case pubapi returns server error" in new Context {

      val serverErrorResponse = responseBuilder(500)
      mothershipDispatcher.dispatch(request) returns serverErrorResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(serverErrorResponse) ==== responseBuilder
      there was one(contentAuthService).findRulesApplicableTo(userSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }


    "should return pubapi response in case pubapi response is successful and content policy is not SNIP" in new Context {

      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.ALLOW

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(successResponse) ==== responseBuilder
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthService).findRulesApplicableTo(userSession, Seq(trackUrn))
    }

    "should return MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new Context {

      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(new ResponseBuilder().status(200).header("Content-Type", "application/json"))
      mediaUrlsRepository.byUrn(userSession, trackUrn, contentAuth) returns mediaUrls
      mapper.map(mediaUrls) returns mapperResponse

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(mapperResponse) ==== responseBuilder

      there was one(mediaUrlsRepository).byUrn(userSession, trackUrn, contentAuth)
      there was one(contentAuthService).findRulesApplicableTo(userSession, Seq(trackUrn))
    }
  }
}
