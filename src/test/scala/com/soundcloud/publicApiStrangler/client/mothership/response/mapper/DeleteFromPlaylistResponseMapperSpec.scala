package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{InvalidUrnDeleteFromPlaylistResponse, NotAuthorizedDeleteFromPlaylistResponse, OkDeleteFromPlaylistResponse}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, Json}

class DeleteFromPlaylistResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val deleteFromPlaylistResponseMapper = new DeleteFromPlaylistResponseMapper
    val status: Status
    val json = moshiPlaylist.as[JsObject]
    lazy val response = JsonResponseBuilder().status(status).body(Json.stringify(json)).build

    def deletedFromPlaylistResponse = deleteFromPlaylistResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val status = Status.Ok
    }

    "returns ok" in new SuccessContext {
      deletedFromPlaylistResponse ==== OkDeleteFromPlaylistResponse
    }
  }

  "when unauthorized response" >> {
    trait UnauthorizedContext extends Context {
      override val status = Status.Unauthorized
    }

    "returns unauthorized" in new UnauthorizedContext {
      deletedFromPlaylistResponse ==== NotAuthorizedDeleteFromPlaylistResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val status = Status.NotFound
    }

    "returns invalid urn" in new NotFoundContext {
      deletedFromPlaylistResponse ==== InvalidUrnDeleteFromPlaylistResponse
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val status = Status.BadRequest
    }

    "throws exception" in new InvalidContext {
      deletedFromPlaylistResponse must throwA[UnhandledResponseException]
    }
  }
}
