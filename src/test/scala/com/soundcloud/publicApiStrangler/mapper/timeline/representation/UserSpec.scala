package com.soundcloud.publicApiStrangler.mapper.timeline.representation

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures.withContentsOf
import play.api.libs.json.Json

class UserSpec extends UnitSpecification {
  implicit val context = mock[MappingContext]

  val json = withContentsOf("okidoki", "users_with_deprecated_counts")(0).get
  val baseUrl = ""
  val maybeFollowCounts = None

  val user = new User(json, baseUrl, maybeFollowCounts, maybeRepostsCount = None)

  """exposes "deprecated" count fields from Okidoki""" in new Scope {
    user.playlist_count ==== Some(10001)
    user.likes_count ==== Some(10010)
    user.comments_count ==== Some(10100)
    user.reposts_count ==== Some(11000)
  }

  "it serializes" in new Scope {
    Json.toJson(user) ==== Json.parse(
      """
        |{
        |  "avatar_url": "https://i1.sndcdn.com/avatars-000186314139-46cthb-large.jpg",
        |  "id": 123,
        |  "kind": "user",
        |  "permalink_url": "http://soundcloud.com/adeline",
        |  "uri": "/users/123",
        |  "username": "adeline",
        |  "permalink": "adeline",
        |  "last_modified": "2016/03/08 12:13:23 +0000",
        |  "first_name": "Adeline",
        |  "last_name": "",
        |  "full_name": "Adeline",
        |  "city": "Ibiza/Barcelona",
        |  "description": "All booking at booking@adelinemusic.com",
        |  "country": null,
        |  "track_count": 61,
        |  "public_favorites_count": 10010,
        |  "followers_count": 0,
        |  "followings_count": 0,
        |  "plan": "Pro Plus",
        |  "myspace_name": null,
        |  "discogs_name": null,
        |  "website_title": null,
        |  "website": null,
        |  "reposts_count": 11000,
        |  "comments_count": 10100,
        |  "online": false,
        |  "likes_count": 10010,
        |  "playlist_count": 10001
        |}
      """.stripMargin)
  }
}
