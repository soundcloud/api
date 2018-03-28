package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.client.reposts.{Reposts, RepostsClient}
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future
import play.api.libs.json.Json

class RepostsHandlerSpec extends UnitSpecification {

  trait Context extends HandlerSpecificationScope {
    val user = Urn("soundcloud:users:999")
    val track = Urn("soundcloud:tracks:100")
    val playlist = Urn("soundcloud:playlists:200")
    val geo = new Geo("US")
    val baseUrl = "http://api.example.com"
    val requestHeaders = Map("Host" -> "api.example.com")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    val repostsClient = mock[RepostsClient]

    lazy val handler = new RepostsHandler(new FakeUserAuthentication(session), repostsClient)

    override def routingDefinitions = Routing.forRepostsHandler(handler)
  }

  "PUT /e1/me/track_reposts/:id" >> {
    trait CreateTrackContext extends Context {
      def result: Result

      repostsClient
        .createRepost(session, track, baseUrl)
        .returns(Future.value(result))

      lazy val response = put(handler.createTracksRepost, "/e1/me/track_reposts/100", Map(), requestHeaders)
    }

    "when creating succeeds" in new CreateTrackContext {
      override def result = Created

      response.status ==== Status.Created
      response.contentString.length ==== 0
    }

    "when creating fails because the track does not exist" in new CreateTrackContext {
      override def result = NotFound

      response.status ==== Status.NotFound
      response.contentString.length ==== 0
    }

    "when creating fails because the track was already reposted" in new CreateTrackContext {
      override def result = AlreadyExists

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when creating fails because the user was blocked for spam" in new CreateTrackContext {
      override def result = SpamBlocked

      response.status ==== Status.TooManyRequests
    }

    "when creating fails because of an unknown reason" in new CreateTrackContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
      response.contentString.length ==== 0
    }
  }

  "DELETE /e1/me/track_reposts/:id" >> {
    trait DeleteTrackContext extends Context {
      def result: Result

      repostsClient
        .deleteRepost(session, track, baseUrl)
        .returns(Future.value(result))

      lazy val response = delete(handler.deleteTracksRepost, "/e1/me/track_reposts/100", Map(), requestHeaders)
    }

    "when deleting succeeds" in new DeleteTrackContext {
      override def result = Deleted

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when deleting fails because the track/repost does not exist" in new DeleteTrackContext {
      override def result = NotFound

      response.status ==== Status.NotFound
      response.contentString.length ==== 0
    }

    "when deleting fails because of an unknown reason" in new DeleteTrackContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
      response.contentString.length ==== 0
    }
  }

  "PUT /e1/me/playlist_reposts/:id" >> {
    trait CreatePlaylistContext extends Context {
      def result: Result

      repostsClient
        .createRepost(session, playlist, baseUrl)
        .returns(Future.value(result))

      lazy val response = put(handler.createPlaylistsRepost, "/e1/me/playlist_reposts/200", Map(), requestHeaders)
    }

    "when creating succeeds" in new CreatePlaylistContext {
      override def result = Created

      response.status ==== Status.Created
      response.contentString.length ==== 0
    }

    "when creating fails because the playlist does not exist" in new CreatePlaylistContext {
      override def result = NotFound

      response.status ==== Status.NotFound
      response.contentString.length ==== 0
    }

    "when creating fails because the playlist was already reposted" in new CreatePlaylistContext {
      override def result = AlreadyExists

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when creating fails because the user was blocked for spam" in new CreatePlaylistContext {
      override def result = SpamBlocked

      response.status ==== Status.TooManyRequests
    }

    "when creating fails because of an unknown reason" in new CreatePlaylistContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
      response.contentString.length ==== 0
    }
  }

  "DELETE /e1/me/playlist_reposts/:id" >> {
    trait DeletePlaylistContext extends Context {
      def result: Result

      repostsClient
        .deleteRepost(session, playlist, baseUrl)
        .returns(Future.value(result))

      lazy val response = delete(handler.deletePlaylistsRepost, "/e1/me/playlist_reposts/200", Map(), requestHeaders)
    }

    "when deleting succeeds" in new DeletePlaylistContext {
      override def result = Deleted

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when deleting fails because the playlist/repost does not exist" in new DeletePlaylistContext {
      override def result = NotFound

      response.status ==== Status.NotFound
      response.contentString.length ==== 0
    }

    "when deleting fails because of an unknown reason" in new DeletePlaylistContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
      response.contentString.length ==== 0
    }
  }

  "GET /e1/me/track_reposts/ids" >> {
    trait OnePageRepostedTracksContext extends Context {
      repostsClient
        .trackReposts(session, user, RepostsHandler.UpstreamLimit, None)
        .returns(Future.value(Reposts(List(track), None)))
    }

    trait MultiPageRepostedTracksContext extends Context {
      val track2 = Urn("soundcloud:tracks:101")

      repostsClient
        .trackReposts(session, user, RepostsHandler.UpstreamLimit, None)
        .returns(Future.value(Reposts(List(track), Some("foobar"))))

      repostsClient
        .trackReposts(session, user, RepostsHandler.UpstreamLimit, Some("foobar"))
        .returns(Future.value(Reposts(List(track2), None)))
    }

    "with linked_partitioning disabled" >> {
      "when one page of track reposts is available" >> {
        "it returns 200 OK" in new OnePageRepostedTracksContext {
          get(handler.getUserRepostableTracks, "/e1/me/track_reposts/ids", Map(), requestHeaders).status ==== Status.Ok
        }

        "it returns a list of track IDs" in new OnePageRepostedTracksContext {
          val response = get(handler.getUserRepostableTracks, "/e1/me/track_reposts/ids", Map(), requestHeaders)
          Json.parse(response.contentString).as[List[Long]] ==== List(track.getIdentifier.toLong)
        }
      }

      "when two pages of track reposts are available" >> {
        "it returns 200 OK" in new MultiPageRepostedTracksContext {
          get(handler.getUserRepostableTracks, "/e1/me/track_reposts/ids", Map(), requestHeaders).status ==== Status.Ok
        }

        "it returns a list of track IDs" in new MultiPageRepostedTracksContext {
          val response = get(handler.getUserRepostableTracks, "/e1/me/track_reposts/ids", Map(), requestHeaders)
          Json.parse(response.contentString).as[List[Long]] ==== List(track, track2).map(_.getIdentifier.toLong)
        }
      }

      "when the limit is not in range" >> {
        "it returns 400 Bad Request" in new OnePageRepostedTracksContext {
          get(handler.getUserRepostableTracks, "/e1/me/track_reposts/ids", Map("limit" -> "10000"), requestHeaders).status ==== Status.BadRequest
        }
      }
    }

    "with linked_partitioning enabled" >> {
      "when a next page is available" >> {
        trait MultiPageLinkedPartitioningContext extends MultiPageRepostedTracksContext {
          val response = get(
            handler.getUserRepostableTracks,
            "/e1/me/track_reposts/ids",
            Map(
              "linked_partitioning" -> "1",
              "limit" -> "1",
              "extraparam" -> "bazbaz"
            ),
            requestHeaders
          )
        }

        "it returns 200 OK" in new MultiPageLinkedPartitioningContext {
          response.status ==== Status.Ok
        }

        "it returns a collection with a list of track IDs" in new MultiPageLinkedPartitioningContext {
          (Json.parse(response.contentString) \ "collection").as[List[Long]] ==== List(track.getIdentifier.toLong)
        }

        "it returns a next_href and includes extra parameters" in new MultiPageLinkedPartitioningContext {
          (Json.parse(response.contentString) \ "next_href").as[String] ==== "http://api.example.com/e1/me/track_reposts/ids?limit=1&extraparam=bazbaz&linked_partitioning=1&cursor=foobar"
        }
      }

      "when a next page is not available" >> {
        trait SinglePageLinkedPartitioningContext extends MultiPageRepostedTracksContext {
          val response = get(
            handler.getUserRepostableTracks,
            "/e1/me/track_reposts/ids",
            Map(
              "linked_partitioning" -> "1",
              "limit" -> "10",
              "extraparam" -> "bazbaz"
            ),
            requestHeaders
          )
        }

        "it returns 200 OK" in new SinglePageLinkedPartitioningContext {
          response.status ==== Status.Ok
        }

        "it returns a collection with a list of track IDs" in new SinglePageLinkedPartitioningContext {
          (Json.parse(response.contentString) \ "collection").as[List[Long]] ==== List(track, track2).map(_.getIdentifier.toLong)
        }

        "it doesn't return a next_href" in new SinglePageLinkedPartitioningContext {
          (Json.parse(response.contentString) \ "next_href").asOpt[String] should beEmpty
        }
      }
    }
  }

  "GET /e1/me/playlist_reposts/ids" >> {
    "when one page of playlist reposts is available" >> {
      trait OnePageRepostedPlaylistsContext extends Context {
        repostsClient
          .playlistReposts(session, user, RepostsHandler.UpstreamLimit, None)
          .returns(Future.value(Reposts(List(playlist), None)))
      }

      "it returns 200 OK" in new OnePageRepostedPlaylistsContext {
        get(handler.getUserRepostablePlaylists, "/e1/me/playlist_reposts/ids", Map(), requestHeaders).status ==== Status.Ok
      }

      "it returns a list of playlist IDs" in new OnePageRepostedPlaylistsContext {
        val response = get(handler.getUserRepostablePlaylists, "/e1/me/playlist_reposts/ids", Map(), requestHeaders)
        Json.parse(response.contentString).as[List[Long]] ==== List(playlist.getIdentifier.toLong)
      }
    }

    "when two pages of playlist reposts are available" >> {
      trait MultiPageRepostedPlaylistsContext extends Context {
        val playlist2 = Urn("soundcloud:playlists:201")

        repostsClient
          .playlistReposts(session, user, RepostsHandler.UpstreamLimit, None)
          .returns(Future.value(Reposts(List(playlist), Some("foobar"))))

        repostsClient
          .playlistReposts(session, user, RepostsHandler.UpstreamLimit, Some("foobar"))
          .returns(Future.value(Reposts(List(playlist2), None)))
      }

      "it returns 200 OK" in new MultiPageRepostedPlaylistsContext {
        get(handler.getUserRepostablePlaylists, "/e1/me/playlist_reposts/ids", Map(), requestHeaders).status ==== Status.Ok
      }

      "it returns a list of playlist IDs" in new MultiPageRepostedPlaylistsContext {
        val response = get(handler.getUserRepostablePlaylists, "/e1/me/playlist_reposts/ids", Map(), requestHeaders)
        Json.parse(response.contentString).as[List[Long]] ==== List(playlist, playlist2).map(_.getIdentifier.toLong)
      }
    }
  }
}
