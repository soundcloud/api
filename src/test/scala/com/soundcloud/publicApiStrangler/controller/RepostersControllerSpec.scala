package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser
import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.reposts.{RepostsClient, Reposts}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Geo
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

class RepostersControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val user = Urn("soundcloud:users:999")
    val track = Urn("soundcloud:tracks:100")
    val playlist = Urn("soundcloud:playlists:200")
    val geo = Geo("US")
    val baseUrl = "http://api.example.com"
    val requestHeaders = Map("Host" -> "api.example.com")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    val repostsClient = mock[RepostsClient]
    val okidokiClient = mock[RichOkidokiClient]
    val followCountsClient = mock[FollowCountsClient]
    val lieblingClient = mock[LieblingClient]

    lazy val controller = new RepostersController(fakeUserAuthentication(session), repostsClient, okidokiClient, followCountsClient, lieblingClient, () => Future.False, () => Future.False)
  }

  "track reposters" >> {
    trait TrackReposters extends Context {
      val trackReposts = Reposts(List(user), Some("ohai"))
      val okidokiUsersJson = withContentsOf("okidoki", "users").as[JsArray]
      val okidokiUserResponse = RepostsUser(okidokiUsersJson(0),
                                            baseUrl,
                                            None,
                                            None,
                                            None)(new MappingContext(session))
      repostsClient
        .reposters(session, track, 200, None)
        .returns(Future.value(trackReposts))

      repostsClient
        .reposters(session, track, 1, Some("hallo"))
        .returns(Future.value(trackReposts))

      okidokiClient
        .fetchRepostsUsersWithoutCounts(session, Set(user), baseUrl, 50)
        .returns(Future.value(List(okidokiUserResponse)))

      followCountsClient
        .counts(session, List(user))
        .returns(Future.value(List(FollowCounts(user, 1, 2))))
    }

    "returns result array if no linked_partitioning param" in new TrackReposters {
      val responseFixture = Json.parse("""
[
    {
        "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        "id": 123,
        "kind": "user",
        "permalink_url": "http://soundcloud.com/adeline",
        "uri": "http://api.example.com/users/123",
        "username": "adeline",
        "permalink": "adeline",
        "last_modified": "2014/10/04 10:48:34 +0000",
        "first_name": "Adeline",
        "last_name": null,
        "full_name": "Adeline",
        "city": "London",
        "description": "For Adeline bookings worldwide",
        "country": null,
        "track_count": 49,
        "public_favorites_count": 5,
        "followers_count": 20976,
        "followings_count": 118,
        "plan": "Pro Plus",
        "myspace_name": null,
        "discogs_name": null,
        "website_title": "Adeline Website",
        "website": "http://www.adelinemusic.com",
        "reposts_count": null,
        "comments_count": null,
        "online": false,
        "likes_count": 5,
        "playlist_count": null
    }
]""")
      val response = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters", Map.empty, requestHeaders)
      val responseJson = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters.json", Map.empty, requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }

    "returns result object if linked_partitioning param is present" in new TrackReposters {
      val responseFixture = Json.parse("""
{
    "collection": [
        {
            "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
            "id": 123,
            "kind": "user",
            "permalink_url": "http://soundcloud.com/adeline",
            "uri": "http://api.example.com/users/123",
            "username": "adeline",
            "permalink": "adeline",
            "last_modified": "2014/10/04 10:48:34 +0000",
            "first_name": "Adeline",
            "last_name": null,
            "full_name": "Adeline",
            "city": "London",
            "description": "For Adeline bookings worldwide",
            "country": null,
            "track_count": 49,
            "public_favorites_count": 5,
            "followers_count": 20976,
            "followings_count": 118,
            "plan": "Pro Plus",
            "myspace_name": null,
            "discogs_name": null,
            "website_title": "Adeline Website",
            "website": "http://www.adelinemusic.com",
            "reposts_count": null,
            "comments_count": null,
            "online": false,
            "likes_count": 5,
            "playlist_count": null
        }
    ],
    "next_href": "http://api.example.com/e1/tracks/100/reposters?linked_partitioning=1&limit=200&cursor=ohai"
}""")
      val response = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1"), requestHeaders)
      val responseJson = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1"), requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }

    "respects limit/cursor params" in new TrackReposters {
      val responseFixture = Json.parse("""
{
    "collection": [
        {
            "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
            "id": 123,
            "kind": "user",
            "permalink_url": "http://soundcloud.com/adeline",
            "uri": "http://api.example.com/users/123",
            "username": "adeline",
            "permalink": "adeline",
            "last_modified": "2014/10/04 10:48:34 +0000",
            "first_name": "Adeline",
            "last_name": null,
            "full_name": "Adeline",
            "city": "London",
            "description": "For Adeline bookings worldwide",
            "country": null,
            "track_count": 49,
            "public_favorites_count": 5,
            "followers_count": 20976,
            "followings_count": 118,
            "plan": "Pro Plus",
            "myspace_name": null,
            "discogs_name": null,
            "website_title": "Adeline Website",
            "website": "http://www.adelinemusic.com",
            "reposts_count": null,
            "comments_count": null,
            "online": false,
            "likes_count": 5,
            "playlist_count": null
        }
    ],
    "next_href": "http://api.example.com/e1/tracks/100/reposters?linked_partitioning=1&limit=1&cursor=ohai"
}""")
      val response = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo"), requestHeaders)
      val responseJson = get(controller, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo"), requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }
  }

  "playlist reposters" >> {
    trait PlaylistReposters extends Context {
      val playlistReposts = Reposts(List(user), Some("ohai"))
      val okidokiUsersJson = withContentsOf("okidoki", "users").as[JsArray]
      val okidokiUserResponse = RepostsUser(okidokiUsersJson(0),
                                            baseUrl,
                                            None,
                                            None,
                                            None)(new MappingContext(session))
      repostsClient
        .reposters(session, playlist, 200, None)
        .returns(Future.value(playlistReposts))

      repostsClient
        .reposters(session, playlist, 1, Some("hallo"))
        .returns(Future.value(playlistReposts))

      okidokiClient
        .fetchRepostsUsersWithoutCounts(session, Set(user), baseUrl, 50)
        .returns(Future.value(List(okidokiUserResponse)))

      followCountsClient
        .counts(session, List(user))
        .returns(Future.value(List(FollowCounts(user, 1, 2))))
    }

    "returns result array if no linked_partitioning param" in new PlaylistReposters {
      val responseFixture = Json.parse("""
[
    {
        "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
        "id": 123,
        "kind": "user",
        "permalink_url": "http://soundcloud.com/adeline",
        "uri": "http://api.example.com/users/123",
        "username": "adeline",
        "permalink": "adeline",
        "last_modified": "2014/10/04 10:48:34 +0000",
        "first_name": "Adeline",
        "last_name": null,
        "full_name": "Adeline",
        "city": "London",
        "description": "For Adeline bookings worldwide",
        "country": null,
        "track_count": 49,
        "public_favorites_count": 5,
        "followers_count": 20976,
        "followings_count": 118,
        "plan": "Pro Plus",
        "myspace_name": null,
        "discogs_name": null,
        "website_title": "Adeline Website",
        "website": "http://www.adelinemusic.com",
        "reposts_count": null,
        "comments_count": null,
        "online": false,
        "likes_count": 5,
        "playlist_count": null
    }
]""")
      val response = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map.empty, requestHeaders)
      val responseJson = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters.json", Map.empty, requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }

    "returns result object if linked_partitioning param is present" in new PlaylistReposters {
      val responseFixture = Json.parse("""
{
    "collection": [
        {
            "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
            "id": 123,
            "kind": "user",
            "permalink_url": "http://soundcloud.com/adeline",
            "uri": "http://api.example.com/users/123",
            "username": "adeline",
            "permalink": "adeline",
            "last_modified": "2014/10/04 10:48:34 +0000",
            "first_name": "Adeline",
            "last_name": null,
            "full_name": "Adeline",
            "city": "London",
            "description": "For Adeline bookings worldwide",
            "country": null,
            "track_count": 49,
            "public_favorites_count": 5,
            "followers_count": 20976,
            "followings_count": 118,
            "plan": "Pro Plus",
            "myspace_name": null,
            "discogs_name": null,
            "website_title": "Adeline Website",
            "website": "http://www.adelinemusic.com",
            "reposts_count": null,
            "comments_count": null,
            "online": false,
            "likes_count": 5,
            "playlist_count": null
        }
    ],
    "next_href": "http://api.example.com/e1/playlists/200/reposters?linked_partitioning=1&limit=200&cursor=ohai"
}""")
      val response = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1"), requestHeaders)
      val responseJson = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1"), requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }

    "respects limit/cursor params" in new PlaylistReposters {
      val responseFixture = Json.parse("""
{
    "collection": [
        {
            "avatar_url": "https://i1.sndcdn.com/avatars-000092704388-h04iht-large.jpg?86347b7",
            "id": 123,
            "kind": "user",
            "permalink_url": "http://soundcloud.com/adeline",
            "uri": "http://api.example.com/users/123",
            "username": "adeline",
            "permalink": "adeline",
            "last_modified": "2014/10/04 10:48:34 +0000",
            "first_name": "Adeline",
            "last_name": null,
            "full_name": "Adeline",
            "city": "London",
            "description": "For Adeline bookings worldwide",
            "country": null,
            "track_count": 49,
            "public_favorites_count": 5,
            "followers_count": 20976,
            "followings_count": 118,
            "plan": "Pro Plus",
            "myspace_name": null,
            "discogs_name": null,
            "website_title": "Adeline Website",
            "website": "http://www.adelinemusic.com",
            "reposts_count": null,
            "comments_count": null,
            "online": false,
            "likes_count": 5,
            "playlist_count": null
        }
    ],
    "next_href": "http://api.example.com/e1/playlists/200/reposters?linked_partitioning=1&limit=1&cursor=ohai"
}""")
      val response = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo"), requestHeaders)
      val responseJson = get(controller, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo"), requestHeaders)

      response.status ==== responseJson.status
      response.jsonBody ==== responseJson.jsonBody

      response.status ==== Status.Ok
      response.jsonBody ==== responseFixture
    }
  }
}
