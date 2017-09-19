package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, ResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.authorization.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.publicApiStrangler.client.media.{MediaUrl, MediaUrlsRepository}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http._
import com.twitter.util.{Await, Future}
import play.api.libs.json.Json

class TrackStreamHandlerSpec extends UnitSpecification {

  "TrackStreamHandler" should {
    trait Context extends Scope {
      val mothershipDispatcher = mock[DispatchToMothershipHandler]
      val contentAuthRules = mock[ContentAuthorizationRules]
      val mediaUrlsRepository = mock[MediaUrlsRepository]
      val handler = new TrackStreamHandler(mothershipDispatcher, contentAuthRules, mediaUrlsRepository)
      val request = mock[HandlerRequest]
      val innerRequest = mock[Request]

      request.request returns innerRequest
      innerRequest.method returns Method.Get

      val mapper = mock[TrackStreamResponseMapper]
      val contentAuthorizationSnip = new ContentAuthorization(Urn("soundcloud:tracks:123"), ContentPolicy.SNIP, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
      val contentAuthorizationAllow = new ContentAuthorization(Urn("soundcloud:tracks:123"), ContentPolicy.ALLOW, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
      val contentAuthorizationMonetize = new ContentAuthorization(Urn("soundcloud:tracks:123"), ContentPolicy.MONETIZE, Reason.DEFAULT, MonetizationModel.NOT_APPLICABLE)
      val contentAuthorizationGeoBLock = new ContentAuthorization(Urn("soundcloud:tracks:123"), ContentPolicy.BLOCK, Reason.GEO, MonetizationModel.NOT_APPLICABLE)

      def responseBuilder(status: Status) =
        Future.value(ResponseBuilder().status(status).build)
    }


    trait ValidUrnContextAnonymous extends Context {
      val anonUserSession = new UserSessionBuilder().build
      val trackId = "334030"
      val paramMap = ParamMap("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)
    }

    trait ValidUrnContextIdentified extends Context {
      val identifiedUserSesssion = new UserSessionBuilder().setUser(Urn("soundcloud:users:123")).build
      val trackId = "334030"
      val paramMap = ParamMap("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)
    }

    trait NonNumericUrnContext extends Context {
      val anonUserSession = new UserSessionBuilder().build
      val trackId = "non-numeric"
      val paramMap = ParamMap("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)
    }

    "when the URN contains invalid characters" >> {

      trait InvalidUrnContext extends Context {
        val anonUserSession = new UserSessionBuilder().build
        val trackId = "non-numeric"
        val paramMap = ParamMap("trackId" -> "1298!!!!")
        request.routeParams returns paramMap
        val trackUrn = new Urn("soundcloud", "tracks", trackId)
      }

      "a 404 is returned" in new InvalidUrnContext {
        val response = Await.result(handler.handle(request, anonUserSession, mapper))
        response.status ==== Status.NotFound
      }
    }

    "return Mothership 404 if id is not numeric" in new NonNumericUrnContext {
      val response = Await.result(handler.handle(request, anonUserSession, mapper))

      response.status ==== Status.NotFound
      // Mothership headers
      response.headerMap.get("Status") ==== Some("404 Not Found")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json;charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"404 - Not Found\"}]}"
      there was noCallsTo(contentAuthRules)
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi returns client error" in new ValidUrnContextAnonymous {
      val clientErrorResponse = responseBuilder(Status.Unauthorized)
      mothershipDispatcher.dispatch(request) returns clientErrorResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(clientErrorResponse) ==== responseBuilder
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi returns server error" in new ValidUrnContextAnonymous {
      val serverErrorResponse = responseBuilder(Status.InternalServerError)
      mothershipDispatcher.dispatch(request) returns serverErrorResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(serverErrorResponse) ==== responseBuilder
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi response is successful and ContentPolicy = ALLOW" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationAllow))

      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(successResponse) ==== responseBuilder
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "return pubapi response in case pubapi response is successful and ContentPolicy = MONETIZE" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationMonetize))

      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(successResponse) ==== responseBuilder
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "HEAD return MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(ResponseBuilder.ok())
      mediaUrlsRepository.byUrn(anonUserSession, trackUrn, contentAuthorizationSnip) returns mediaUrls
      mapper.map(mediaUrls, true) returns mapperResponse

      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "http"))

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, anonUserSession, mapper))
      response ==== Await.result(mapperResponse)
    }

    "GET return MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(ResponseBuilder.ok())
      mediaUrlsRepository.byUrn(anonUserSession, trackUrn, contentAuthorizationSnip) returns mediaUrls
      mapper.map(mediaUrls, false) returns mapperResponse

      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "http"))
      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(mapperResponse) ==== responseBuilder

      there was one(mediaUrlsRepository).byUrn(anonUserSession, trackUrn, contentAuthorizationSnip)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "HEAD return https MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new ValidUrnContextAnonymous {
      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "https"))
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(ResponseBuilder.ok())
      mediaUrlsRepository.byUrn(anonUserSession, trackUrn, contentAuthorizationSnip, true) returns mediaUrls
      mapper.map(mediaUrls, true) returns mapperResponse

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, anonUserSession, mapper))
      response ==== Await.result(mapperResponse)

      there was one(mediaUrlsRepository).byUrn(anonUserSession, trackUrn, contentAuthorizationSnip, true)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "GET return https MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new ValidUrnContextAnonymous {
      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "https"))
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationSnip))

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(ResponseBuilder.ok())
      mediaUrlsRepository.byUrn(anonUserSession, trackUrn, contentAuthorizationSnip, true) returns mediaUrls
      mapper.map(mediaUrls, false) returns mapperResponse

      val responseBuilder = Await.result(handler.handle(request, anonUserSession, mapper))
      Await.result(mapperResponse) ==== responseBuilder

      there was one(mediaUrlsRepository).byUrn(anonUserSession, trackUrn, contentAuthorizationSnip, true)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "HEAD return 401 - Unauthorized when content policy is BLOCK and anonymous user" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationGeoBLock))

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, anonUserSession, mapper))
      response.status ==== Status.Unauthorized

      response.headerMap.get("Status") ==== Some("401 Unauthorized")
      response.headerMap.get("Date") must not be None

      // Must have an empty response body.
      response.headerMap.get("Content-Type") ==== Some("text/plain;charset=utf-8")
      response.headerMap.get("Content-Length") ==== Some("0")
      response.contentString ==== ""

      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "GET return 401 - Unauthorized when content policy is BLOCK and anonymous user" in new ValidUrnContextAnonymous {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(anonUserSession, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationGeoBLock))

      val response = Await.result(handler.handle(request, anonUserSession, mapper))
      response.status ==== Status.Unauthorized

      // Mothership headers
      response.headerMap.get("Status") ==== Some("401 Unauthorized")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json;charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"401 - Unauthorized\"}]}"
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(anonUserSession, Seq(trackUrn))
    }

    "HEAD return 403 - Forbidden when content policy is BLOCK and logged in user" in new ValidUrnContextIdentified {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(identifiedUserSesssion, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationGeoBLock))

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, identifiedUserSesssion, mapper))
      response.status ==== Status.Forbidden

      response.headerMap.get("Status") ==== Some("403 Forbidden")
      response.headerMap.get("Date") must not be None

      // Must have an empty response body.
      response.headerMap.get("Content-Type") ==== Some("text/plain;charset=utf-8")
      response.headerMap.get("Content-Length") ==== Some("0")
      response.contentString ==== ""

      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(identifiedUserSesssion, Seq(trackUrn))
    }

    "GET return 403 - Forbidden when content policy is BLOCK and logged in user" in new ValidUrnContextIdentified {
      val successResponse = responseBuilder(Status.Ok)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuthRules.fetchRules(identifiedUserSesssion, Seq(trackUrn)) returns Future.value(Seq(contentAuthorizationGeoBLock))

      val response = Await.result(handler.handle(request, identifiedUserSesssion, mapper))
      response.status ==== Status.Forbidden

      // Mothership headers
      response.headerMap.get("Status") ==== Some("403 Forbidden")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json;charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"403 - Forbidden\"}]}"
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(identifiedUserSesssion, Seq(trackUrn))
    }

  }
}
