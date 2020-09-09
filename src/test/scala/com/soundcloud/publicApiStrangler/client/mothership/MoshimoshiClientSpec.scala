package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.http.{Headers, HeadersBuilder}
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.test.Helpers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import com.twitter.util.Await
import play.api.libs.json.{JsObject, Json, _}

class MoshimoshiClientSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val service = mock[JsonClient]

    implicit val session = loggedInSession(Urn("soundcloud", "users", "1"))
    val client = new MoshimoshiClient(
      service
    )
  }

  def buildHeaders(entries: (String, String)*): Headers =
    entries.foldLeft(new HeadersBuilder()) { case (builder, (key, value)) => builder.set(key, value) }.build()

  // A recent play-json upgrade has introduced a change that does not preserve Map key ordering, thus breaking some
  // specs that rely on comparing JSON as as strings. Since this is limited to only a few specs in this class only,
  // we just re-parse the json and create a fresh object to ensure we have the correct ordering. Sorry.
  // See https://github.com/playframework/play-json/issues/236
  def fixTrackFixture(v: JsValue): JsValue = {
    val track = (v \ "track").as[JsObject]
    Json.obj("track" -> (track ++ Json.obj()))
  }

  "#fetchUserObjects" >> {
    trait UsersContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "10419549"), Urn("soundcloud", "users", "123123123"))

      def path = Path() / "users" / "fetch"

      def fetch = Await.result(client.fetchUserObjects(session, urns))
    }

    "found response" >> {
      "gotta fetch'em all" in new UsersContext {
        expectOkResponse(path, moshiUsers, urns.toList)

        fetch ==== List(UserMapper(moshiUser), UserMapper(moshiUser2))
      }
    }

    "not found response" in new UsersContext {
      expectOkResponse(path, JsArray(), urns.toList)

      fetch ==== List()
    }

    "invalid response" in new UsersContext {
      expectInternalErrorResponse(path, urns.toList)

      fetch must throwA[IllegalStateException]
    }
  }
}
