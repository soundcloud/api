package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  OkResetUserPasswordResponse,
  UserDoesntExistButDontExposeThisResetUserPasswordResponse
}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, Json}

class ResetUserPasswordResponseMapperSpec extends UnitSpecification {
  trait Context extends Scope {
    val resetUserPasswordResponseMapper = new ResetUserPasswordResponseMapper
    val statusCode: Int
    val json = moshiPlaylist.as[JsObject]
    lazy val response = JsonResponseBuilder().status(Status(statusCode)).body(Json.stringify(json)).build

    def resetUserPasswordResponse = resetUserPasswordResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val statusCode = 205
    }

    "returns ok" in new SuccessContext {
      resetUserPasswordResponse ==== OkResetUserPasswordResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val statusCode = 404
    }

    "returns does not exists" in new NotFoundContext {
      resetUserPasswordResponse ==== UserDoesntExistButDontExposeThisResetUserPasswordResponse
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val statusCode = 400
    }

    "throws exception" in new InvalidContext {
      resetUserPasswordResponse must throwA[UnhandledResponseException]
    }
  }
}
