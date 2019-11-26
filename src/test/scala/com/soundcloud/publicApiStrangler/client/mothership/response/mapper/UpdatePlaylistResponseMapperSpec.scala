package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{
  InvalidUrnUpdatePlaylistResponse,
  NotAuthorizedUpdatePlaylistResponse,
  OkUpdatePlaylistResponse
}
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.JsObject

class UpdatePlaylistResponseMapperSpec extends UnitSpecification {
  trait Context extends Scope {
    val updatePlaylistResponseMapper = new UpdatePlaylistResponseMapper
    val statusCode: Int
    val json = moshiPlaylist.as[JsObject]
    lazy val response = jsonResponse(new Status(statusCode), json)

    def updatedPlaylistResponse = updatePlaylistResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val statusCode = 200
      val playlist = PlaylistMapper(json)
    }

    "returns ok" in new SuccessContext {
      updatedPlaylistResponse ==== OkUpdatePlaylistResponse(playlist)
    }
  }

  "when unauthorized response" >> {
    trait UnauthorizedContext extends Context {
      override val statusCode = 401
    }

    "returns unauthorized" in new UnauthorizedContext {
      updatedPlaylistResponse ==== NotAuthorizedUpdatePlaylistResponse
    }
  }

  "when not found response" >> {
    trait NotFoundContext extends Context {
      override val statusCode = 404
    }

    "returns invalid urn" in new NotFoundContext {
      updatedPlaylistResponse ==== InvalidUrnUpdatePlaylistResponse
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val statusCode = 400
    }

    "throws exception" in new InvalidContext {
      updatedPlaylistResponse must throwA[UnhandledResponseException]
    }
  }
}
