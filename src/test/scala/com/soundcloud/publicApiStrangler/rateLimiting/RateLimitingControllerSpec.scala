package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.scalakit.Urn
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.HttpResponseStatus
import play.api.libs.json.Json

import scala.collection.immutable.TreeSet

class RateLimitingControllerSpec extends InjectionBasedControllerSpecification {

  "The RateLimitingController" should {

    trait Context extends Scope {
      val whitelistingServiceMock = mock[WhitelistingService]
      val controller = new RateLimitingController(whitelistingServiceMock)
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
  }
}
