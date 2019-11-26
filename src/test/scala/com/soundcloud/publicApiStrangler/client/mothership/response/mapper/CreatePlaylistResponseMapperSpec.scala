package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.client.support.UnhandledResponseException
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.finagle.http.Status
import play.api.libs.json.{JsObject, Json}

class CreatePlaylistResponseMapperSpec extends UnitSpecification {
  trait Context extends Scope {
    val createPlaylistResponseMapper = new CreatePlaylistResponseMapper
    val status: Status
    val json = moshiPlaylist.as[JsObject]
    lazy val response = JsonResponseBuilder().status(status).body(Json.stringify(json)).build

    def createdPlaylist = createPlaylistResponseMapper(response)
  }

  "when successful response" >> {
    trait SuccessContext extends Context {
      override val status = Status.Created
      val playlist = PlaylistMapper(json)
    }

    "maps to playlist" in new SuccessContext {
      createdPlaylist ==== playlist
    }
  }

  "when invalid response" in {
    trait InvalidContext extends Context {
      override val status = Status.BadRequest
    }

    "throws exception" in new InvalidContext {
      createdPlaylist must throwA[UnhandledResponseException]
    }
  }
}
