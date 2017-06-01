package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{InvalidUrnAddToPlaylistResponse, NotAuthorizedAddToPlaylistResponse, OkAddToPlaylistResponse, TrackAlreadyInPlaylistAddToPlaylistResponse}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, Json}

class AddToPlaylistResponseMapperSpec extends UnitSpecification {

  trait Context extends Scope {
    val addToPlaylistResponseMapper = new AddToPlaylistResponseMapper
    val status: Status
    val json = moshiPlaylist.as[JsObject]
    lazy val response = JsonResponseBuilder().status(status).body(Json.stringify(json)).build

    def addedToPlaylistResponse = addToPlaylistResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val status = Status.Ok
    }

    "returns ok" in new SuccessContext {
      addedToPlaylistResponse ==== OkAddToPlaylistResponse
    }
  }

  "when unauthorized response" >> {
    trait UnauthorizedContext extends Context {
      override val status = Status.Unauthorized
    }

    "returns unauthorized" in new UnauthorizedContext {
      addedToPlaylistResponse ==== NotAuthorizedAddToPlaylistResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val status = Status.NotFound
    }

    "returns invalid urn" in new NotFoundContext {
      addedToPlaylistResponse ==== InvalidUrnAddToPlaylistResponse
    }
  }

  "when conflict response" >> {
    trait ConflictContext extends Context {
      override val status = Status.Conflict
    }

    "returns already in playlist" in new ConflictContext {
      addedToPlaylistResponse ==== TrackAlreadyInPlaylistAddToPlaylistResponse
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val status = Status.BadRequest
    }

    "throws exception" in new InvalidContext {
      addedToPlaylistResponse must throwA[UnhandledResponseException]
    }
  }
}
