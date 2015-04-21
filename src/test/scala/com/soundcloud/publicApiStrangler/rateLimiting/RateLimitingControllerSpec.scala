package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.scalakit.Urn
import com.twitter.util.{Time, Future}
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.{JsNumber, JsString, Json}

import scala.collection.immutable.TreeSet

class RateLimitingControllerSpec extends InjectionBasedControllerSpecification {

  "The RateLimitingController" should {

    trait Context extends Scope {
      val whitelistingServiceMock = mock[WhitelistingService]
      val rateLimiterMock = mock[RateLimiter]
      val controller = new RateLimitingController(whitelistingServiceMock, rateLimiterMock)
    }

    "put a client on its whitelist" in new Context {
      whitelistingServiceMock.whitelistClient(Urn("soundcloud:applications:test")) returns Future(())
      val response = put(controller, "/-/ratelimits/whitelist/soundcloud:applications:test")
      response.status ==== HttpResponseStatus.NO_CONTENT
      response.body ==== ""
    }

    "delete a client from its whitelist" in new Context {
      whitelistingServiceMock.unwhitelistClient(Urn("soundcloud:applications:test")) returns Future(())
      val response = delete(controller, "/-/ratelimits/whitelist/soundcloud:applications:test")
      response.status ==== HttpResponseStatus.NO_CONTENT
      response.body ==== ""
    }

    "get all the clients currently on its whitelist" in new Context {
      whitelistingServiceMock.whitelistedClients returns TreeSet(Urn("soundcloud:applications:test2"), Urn("soundcloud:applications:test1"))
      val response = get(controller, "/-/ratelimits/whitelist")
      val urnsInResponse = Json.parse(response.body).as[Seq[String]]
      urnsInResponse(0) ==== "soundcloud:applications:test1"
      urnsInResponse(1) ==== "soundcloud:applications:test2"
    }

    "get the rate limit status of a client" in new Context {
      rateLimiterMock.rateLimitStatus(any) returns Future.value(RateLimitStatus.Advancing(5, Some(Time.epoch)))
      val response = get(controller, "/-/ratelimits/status/soundcloud:applications:test1")
      val json = Json.parse(response.body)
      json \ "rate_limit_status" ==== JsString("advancing")
      json \ "remaining_requests" ==== JsNumber(5)
      json \ "reset_time" ==== JsString("1970/01/01 12:00:00 +0000")
    }
  }
}
