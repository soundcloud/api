package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.media.{MediaUrl, MediaUrlsRepository}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.policies.{Reason, ContentAuthorization, ContentPolicy}
import com.soundcloud.jvmkit.{Urn, UserSession}
import com.soundcloud.publicApiStrangler.authorization.ContentAuthorizationRules
import com.soundcloud.publicApiStrangler.mapper.trackstreams.TrackStreamResponseMapper
import com.twitter.finagle.http.{Method, HeaderMap, Status}
import com.twitter.util.{Await, Future}
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.Json

class TrackStreamHandlerSpec extends UnitSpecification {

  "TrackStreamHandler" should {
    trait Context extends Scope {
      val mothershipDispatcher = mock[DispatchToMothershipHandler]
      val contentAuthRules = mock[ContentAuthorizationRules]
      val mediaUrlsRepository = mock[MediaUrlsRepository]
      val handler = new TrackStreamHandler(mothershipDispatcher, contentAuthRules, mediaUrlsRepository)
      val request = mock[Request]

      request.method returns Method.Get

      val userSession = mock[UserSession]
      val mapper = mock[TrackStreamResponseMapper]
      val contentAuth = mock[ContentAuthorization]

      def responseBuilder(statusCode: Int) =
        Future.value(new ResponseBuilder().status(statusCode))

      def responseBuilder(statusCode: Int, contentType: String, body: String) =
        new ResponseBuilder().status(statusCode).contentType(contentType).body(body)

    }

    trait Success extends Context {
      val trackId = "334030"
      val paramMap = Map("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)
      contentAuthRules.fetchRules(userSession, Seq(trackUrn)) returns Future.value(Seq(contentAuth))
    }

    trait Failure extends Context {
      val trackId = "non-numeric"
      val paramMap = Map("trackId" -> trackId)
      request.routeParams returns paramMap
      val trackUrn = new Urn("soundcloud", "tracks", trackId)
    }

    "return Mothership 404 if id is not numeric" in new Failure {
      val response = Await.result(handler.handle(request, userSession, mapper)).build

      response.status ==== Status.NotFound
      // Mothership headers
      response.headerMap.get("Status") ==== Some("404 Not Found")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json; charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"404 - Not Found\"}]}"
      there was noCallsTo(contentAuthRules)
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi returns client error" in new Success {
      val clientErrorResponse = responseBuilder(401)
      mothershipDispatcher.dispatch(request) returns clientErrorResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(clientErrorResponse) ==== responseBuilder
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi returns server error" in new Success {
      val serverErrorResponse = responseBuilder(500)
      mothershipDispatcher.dispatch(request) returns serverErrorResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(serverErrorResponse) ==== responseBuilder
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
      there was noCallsTo(mediaUrlsRepository)
    }

    "return pubapi response in case pubapi response is successful and ContentPolicy = ALLOW" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.ALLOW

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(successResponse) ==== responseBuilder
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "return pubapi response in case pubapi response is successful and ContentPolicy = MONETIZE" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.MONETIZE

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(successResponse) ==== responseBuilder
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "HEAD return MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(new ResponseBuilder().status(200).header("Content-Type", "application/json"))
      mediaUrlsRepository.byUrn(userSession, trackUrn, contentAuth) returns mediaUrls
      mapper.map(mediaUrls) returns mapperResponse

      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "http"))

      request.method returns Method.Head

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))

      // Must have an empty response body.
      val response = responseBuilder.build
      response.headerMap.get("Content-Length") ==== Some("0")
      response.headerMap.get("Content-Type") must beNone
      response.contentString ==== ""
    }

    "GET return MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(new ResponseBuilder().status(200).header("Content-Type", "application/json"))
      mediaUrlsRepository.byUrn(userSession, trackUrn, contentAuth) returns mediaUrls
      mapper.map(mediaUrls) returns mapperResponse

      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "http"))
      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(mapperResponse) ==== responseBuilder

      there was one(mediaUrlsRepository).byUrn(userSession, trackUrn, contentAuth)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "HEAD return https MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new Success {
      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "https"))
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(new ResponseBuilder().status(200).header("Content-Type", "application/json"))
      mediaUrlsRepository.byUrn(userSession, trackUrn, contentAuth, true) returns mediaUrls
      mapper.map(mediaUrls) returns mapperResponse

      request.method returns Method.Head

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))

      // Must have an empty response body.
      val response = responseBuilder.build
      response.headerMap.get("Content-Length") ==== Some("0")
      response.headerMap.get("Content-Type") must beNone
      response.contentString ==== ""

      there was one(mediaUrlsRepository).byUrn(userSession, trackUrn, contentAuth, true)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "GET return https MediaUrlsRepository response in case pubapi response is successful and content policy is SNIP" in new Success {
      request.headerMap returns HeaderMap(("x-forwarded-proto" -> "https"))
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.SNIP

      val mediaUrl1 = mock[MediaUrl]
      val mediaUrls = Future.value(Set(mediaUrl1))
      val mapperResponse = Future.value(new ResponseBuilder().status(200).header("Content-Type", "application/json"))
      mediaUrlsRepository.byUrn(userSession, trackUrn, contentAuth, true) returns mediaUrls
      mapper.map(mediaUrls) returns mapperResponse

      val responseBuilder = Await.result(handler.handle(request, userSession, mapper))
      Await.result(mapperResponse) ==== responseBuilder

      there was one(mediaUrlsRepository).byUrn(userSession, trackUrn, contentAuth, true)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "HEAD return 401 - Unauthorized when content policy is BLOCK and anonymous user" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.BLOCK
      contentAuth.getReason returns Reason.GEO
      userSession.isAnonymous returns true

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, userSession, mapper)).build
      response.status ==== Status.Unauthorized

      response.headerMap.get("Status") ==== Some("401 Unauthorized")
      response.headerMap.get("Date") must not be None

      // Must have an empty response body.
      response.headerMap.get("Content-Type") must beNone
      response.headerMap.get("Content-Length") ==== Some("0")
      response.contentString ==== ""

      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "GET return 401 - Unauthorized when content policy is BLOCK and anonymous user" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.BLOCK
      contentAuth.getReason returns Reason.GEO
      userSession.isAnonymous returns true

      val response = Await.result(handler.handle(request, userSession, mapper)).build
      response.status ==== Status.Unauthorized

      // Mothership headers
      response.headerMap.get("Status") ==== Some("401 Unauthorized")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json; charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"401 - Unauthorized\"}]}"
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "HEAD return 403 - Forbidden when content policy is BLOCK and logged in user" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.BLOCK
      contentAuth.getReason returns Reason.UNKNOWN
      userSession.isAnonymous returns false

      request.method returns Method.Head

      val response = Await.result(handler.handle(request, userSession, mapper)).build
      response.status ==== Status.Forbidden

      response.headerMap.get("Status") ==== Some("403 Forbidden")
      response.headerMap.get("Date") must not be None

      // Must have an empty response body.
      response.headerMap.get("Content-Type") must beNone
      response.headerMap.get("Content-Length") ==== Some("0")
      response.contentString ==== ""

      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

    "GET return 403 - Forbidden when content policy is BLOCK and logged in user" in new Success {
      val successResponse = responseBuilder(200)
      mothershipDispatcher.dispatch(request) returns successResponse
      contentAuth.getPolicy returns ContentPolicy.BLOCK
      contentAuth.getReason returns Reason.UNKNOWN
      userSession.isAnonymous returns false

      val response = Await.result(handler.handle(request, userSession, mapper)).build
      response.status ==== Status.Forbidden

      // Mothership headers
      response.headerMap.get("Status") ==== Some("403 Forbidden")
      response.headerMap.get("Date") must not be None
      response.headerMap.get("Content-Type") ==== Some("application/json; charset=utf-8")
      Json.parse(response.contentString) // Make sure we have valid json
      response.contentString ==== "{\"errors\":[{\"error_message\":\"403 - Forbidden\"}]}"
      there was noCallsTo(mediaUrlsRepository)
      there was one(contentAuthRules).fetchRules(userSession, Seq(trackUrn))
    }

  }
}
