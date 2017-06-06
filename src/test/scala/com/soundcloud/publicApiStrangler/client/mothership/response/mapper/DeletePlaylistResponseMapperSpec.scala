package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{ForbiddenDeletePlaylistResponse, InvalidUrnDeletePlaylistResponse, NotAuthorizedDeletePlaylistResponse, OkDeletePlaylistResponse}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, Json}

class DeletePlaylistResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val deletePlaylistResponseMapper = new DeletePlaylistResponseMapper
    val status: Status
    val json = moshiPlaylist.as[JsObject]
    lazy val response = JsonResponseBuilder().status(status).body(Json.stringify(json)).build

    def deletedPlaylistResponse = deletePlaylistResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val status = Status.Ok
    }

    "returns ok" in new SuccessContext {
      deletedPlaylistResponse ==== OkDeletePlaylistResponse
    }
  }

  "when accepted response" >> {
    trait SuccessContext extends Context {
      override val status = Status.Accepted
    }

    "returns ok" in new SuccessContext {
      deletedPlaylistResponse ==== OkDeletePlaylistResponse
    }
  }

  "when unauthorized response" >> {
    trait UnauthorizedContext extends Context {
      override val status = Status.Unauthorized
    }

    "returns unauthorized" in new UnauthorizedContext {
      deletedPlaylistResponse ==== NotAuthorizedDeletePlaylistResponse
    }
  }

  "when forbidden response" in {
    trait ForbiddenContext extends Context {
      override val status = Status.Forbidden
    }

    "throws exception" in new ForbiddenContext {
      deletedPlaylistResponse ==== ForbiddenDeletePlaylistResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val status = Status.NotFound
    }

    "returns invalid urn" in new NotFoundContext {
      deletedPlaylistResponse ==== InvalidUrnDeletePlaylistResponse
    }
  }
}
