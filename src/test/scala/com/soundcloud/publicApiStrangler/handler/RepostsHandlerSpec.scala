package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.publicApiStrangler.Routing
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class RepostsHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val user = Urn("soundcloud", "users", "999")
    val track = Urn("soundcloud", "tracks", "100")
    val playlist = Urn("soundcloud", "playlists", "200")
    val geo = new Geo("US")
    val baseUrl = "http://api.example.com"
    val requestHeaders = Map("Host" -> "api.example.com")
    val session =
      new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()

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

      lazy val response = put("/e1/me/track_reposts/100", Map(), requestHeaders)
    }

    "when creating succeeds" in new CreateTrackContext {
      override def result = Created

      response.status ==== Status.Created
      response.contentString.length ==== 0
    }

    "when creating fails because the track does not exist" in new CreateTrackContext {
      override def result = NotFound

      response.status ==== Status.NotFound
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
    }
  }

  "DELETE /e1/me/track_reposts/:id" >> {
    trait DeleteTrackContext extends Context {
      def result: Result

      repostsClient
        .deleteRepost(session, track, baseUrl)
        .returns(Future.value(result))

      lazy val response = delete("/e1/me/track_reposts/100", Map(), requestHeaders)
    }

    "when deleting succeeds" in new DeleteTrackContext {
      override def result = Deleted

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when deleting fails because the track/repost does not exist" in new DeleteTrackContext {
      override def result = NotFound

      response.status ==== Status.NotFound
    }

    "when deleting fails because of an unknown reason" in new DeleteTrackContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
    }
  }

  "PUT /e1/me/playlist_reposts/:id" >> {
    trait CreatePlaylistContext extends Context {
      def result: Result

      repostsClient
        .createRepost(session, playlist, baseUrl)
        .returns(Future.value(result))

      lazy val response = put("/e1/me/playlist_reposts/200", Map(), requestHeaders)
    }

    "when creating succeeds" in new CreatePlaylistContext {
      override def result = Created

      response.status ==== Status.Created
      response.contentString.length ==== 0
    }

    "when creating fails because the playlist does not exist" in new CreatePlaylistContext {
      override def result = NotFound

      response.status ==== Status.NotFound
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
    }
  }

  "DELETE /e1/me/playlist_reposts/:id" >> {
    trait DeletePlaylistContext extends Context {
      def result: Result

      repostsClient
        .deleteRepost(session, playlist, baseUrl)
        .returns(Future.value(result))

      lazy val response = delete("/e1/me/playlist_reposts/200", Map(), requestHeaders)
    }

    "when deleting succeeds" in new DeletePlaylistContext {
      override def result = Deleted

      response.status ==== Status.Ok
      response.contentString.length ==== 0
    }

    "when deleting fails because the playlist/repost does not exist" in new DeletePlaylistContext {
      override def result = NotFound

      response.status ==== Status.NotFound
    }

    "when deleting fails because of an unknown reason" in new DeletePlaylistContext {
      override def result = Failed

      response.status ==== Status.InternalServerError
    }
  }
}
