package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.RoutingDefinitions
import com.soundcloud.publicApiStrangler.client.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.publicApiStrangler.mapping.reposts.RepostsUser
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json._

class RepostersControllerSpec extends UnitSpecification {

  trait Context extends HandlerSpecificationScope {
    val user = Urn("soundcloud:users:999")
    val track = Urn("soundcloud:tracks:100")
    val playlist = Urn("soundcloud:playlists:200")
    val noNextHrefTrack = Urn("soundcloud:tracks:101")
    val noNextHrefPlaylist = Urn("soundcloud:playlists:201")
    val geo = new Geo("US")
    val baseUrl = "http://api.example.com"
    val requestHeaders = Map("Host" -> "api.example.com")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    val usersJson = Json.parse(
      """
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
        ]
      """)

    val repostsClient = mock[RepostsClient]
    val okidokiClient = mock[RichOkidokiClient]
    val followCountsClient = mock[FollowCountsClient]
    val lieblingClient = mock[LieblingClient]

    lazy val controller = new RepostersController(new FakeUserAuthentication(session), repostsClient, okidokiClient, followCountsClient, lieblingClient, () => Future.False)

    override def routingDefinitions = RoutingDefinitions.forRepostersController(controller)
  }

  "track reposters" >> {
    trait TrackReposters extends Context {
      val trackReposts = Reposts(List(user), Some("ohai"))
      val noNextHrefTrackReposts = Reposts(List(user), None)
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

      repostsClient
        .reposters(session, noNextHrefTrack, 200, None)
        .returns(Future.value(noNextHrefTrackReposts))

      repostsClient
        .getRepostCountsByUrnWithFallback(session, Set(user))
        .returns(Future.value(Map.empty[Urn, Long]))

      okidokiClient
        .fetchRepostsUsersWithoutCounts(session, Set(user), baseUrl, 50)
        .returns(Future.value(List(okidokiUserResponse)))

      followCountsClient
        .counts(session, List(user))
        .returns(Future.value(List(FollowCounts(user, 1, 2))))
    }

    "returns result array if no linked_partitioning param" in new TrackReposters {
      val response = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters", Map.empty, requestHeaders)
      val responseJson = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters.json", Map.empty, requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) ==== Json.parse(responseJson.contentString)

      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== usersJson
    }

    "returns result object if linked_partitioning param is present" in new TrackReposters {
      val response = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)
      val responseJson = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) \ "collection" ==== Json.parse(responseJson.contentString) \ "collection"

      responseJson.status ==== Status.Ok
      (Json.parse(response.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/tracks/100/reposters?limit=200&linked_partitioning=1&foo=bar&cursor=ohai"
      (Json.parse(responseJson.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/tracks/100/reposters.json?limit=200&linked_partitioning=1&foo=bar&cursor=ohai"
    }

    "respects limit/cursor params" in new TrackReposters {
      val response = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo", "foo" -> "bar"), requestHeaders)
      val responseJson = get(controller.trackReposters, s"/e1/tracks/${track.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) \ "collection" ==== Json.parse(responseJson.contentString) \ "collection"

      responseJson.status ==== Status.Ok
      (Json.parse(response.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/tracks/100/reposters?limit=1&linked_partitioning=1&cursor=ohai&foo=bar"
      (Json.parse(responseJson.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/tracks/100/reposters.json?limit=1&linked_partitioning=1&cursor=ohai&foo=bar"
    }

    "does not return empty next_href" in new TrackReposters {
      val response = get(controller.trackReposters, s"/e1/tracks/${noNextHrefTrack.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)
      val responseJson = get(controller.trackReposters, s"/e1/tracks/${noNextHrefTrack.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) ==== Json.parse(responseJson.contentString)

      response.status ==== Status.Ok
      Json.parse(response.contentString) \ "collection" ==== usersJson
      (Json.parse(responseJson.contentString) \ "next_href").asOpt[String] should beEmpty
    }
  }

  "playlist reposters" >> {
    trait PlaylistReposters extends Context {
      val playlistReposts = Reposts(List(user), Some("ohai"))
      val noNextHrefPlaylistReposts = Reposts(List(user), None)
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

      repostsClient
        .reposters(session, noNextHrefPlaylist, 200, None)
        .returns(Future.value(noNextHrefPlaylistReposts))

      repostsClient
        .getRepostCountsByUrnWithFallback(session, Set(user))
        .returns(Future.value(Map.empty[Urn, Long]))

      okidokiClient
        .fetchRepostsUsersWithoutCounts(session, Set(user), baseUrl, 50)
        .returns(Future.value(List(okidokiUserResponse)))

      followCountsClient
        .counts(session, List(user))
        .returns(Future.value(List(FollowCounts(user, 1, 2))))
    }

    "returns result array if no linked_partitioning param" in new PlaylistReposters {
      val response = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map.empty, requestHeaders)
      val responseJson = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters.json", Map.empty, requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) ==== Json.parse(responseJson.contentString)

      response.status ==== Status.Ok
      Json.parse(response.contentString) ==== usersJson
    }

    "returns result object if linked_partitioning param is present" in new PlaylistReposters {
      val response = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)
      val responseJson = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) \ "collection" ==== Json.parse(responseJson.contentString) \ "collection"

      responseJson.status ==== Status.Ok
      (Json.parse(response.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/playlists/200/reposters?limit=200&linked_partitioning=1&foo=bar&cursor=ohai"
      (Json.parse(responseJson.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/playlists/200/reposters.json?limit=200&linked_partitioning=1&foo=bar&cursor=ohai"
    }

    "respects limit/cursor params" in new PlaylistReposters {
      val responseJson = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo", "foo" -> "bar"), requestHeaders)
      val response = get(controller.playlistReposters, s"/e1/playlists/${playlist.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "limit" -> "1", "cursor" -> "hallo", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) \ "collection" ==== Json.parse(responseJson.contentString) \ "collection"

      responseJson.status ==== Status.Ok
      (Json.parse(response.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/playlists/200/reposters?limit=1&linked_partitioning=1&cursor=ohai&foo=bar"
      (Json.parse(responseJson.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/playlists/200/reposters.json?limit=1&linked_partitioning=1&cursor=ohai&foo=bar"
    }

    "does not return empty next_href" in new PlaylistReposters {
      val response = get(controller.playlistReposters, s"/e1/playlists/${noNextHrefPlaylist.getIdentifier}/reposters", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)
      val responseJson = get(controller.playlistReposters, s"/e1/playlists/${noNextHrefPlaylist.getIdentifier}/reposters.json", Map("linked_partitioning" -> "1", "foo" -> "bar"), requestHeaders)

      response.status ==== responseJson.status
      Json.parse(response.contentString) ==== Json.parse(responseJson.contentString)

      response.status ==== Status.Ok
      Json.parse(response.contentString) \ "collection" ==== usersJson
      (Json.parse(responseJson.contentString) \ "next_href").asOpt[String] should beEmpty
    }
  }
}
