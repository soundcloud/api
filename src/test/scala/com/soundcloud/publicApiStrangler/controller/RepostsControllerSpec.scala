package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.test.InjectionBasedControllerSpecification
import com.soundcloud.jvmkit.{Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.scalakit.Geo
import com.twitter.finagle.http.Status
import com.twitter.util.Future

class RepostsControllerSpec extends InjectionBasedControllerSpecification {

  trait Context extends Scope {
    val user = Urn("soundcloud:users:999")
    val track = Urn("soundcloud:tracks:100")
    val playlist = Urn("soundcloud:playlists:200")
    val geo = Geo("US")
    val baseUrl = "http://api.example.com"
    val requestHeaders = Map("Host" -> "api.example.com")
    val session = new UserSessionBuilder().setUser(user).setAgent(Urn("soundcloud:applications:v2")).setGeo(geo).build()

    val repostsClient = mock[RepostsClient]
    val fallback = mock[DispatchToMothershipHandler]

    def writeToReposts(): Boolean

    lazy val controller = new RepostsController(fakeUserAuthentication(session), repostsClient, fallback, () => Future.value(writeToReposts()))
  }

  "when write flag is turned off" >> {
    trait WriteFlagOff extends Context {
      override def writeToReposts(): Boolean = false

      fallback.dispatch(any[Request]).returns(Future.value(new ResponseBuilder().status(200)))
    }

    "PUT /e1/me/track_reposts/:id" >> {
      "falls back to mothership" in new WriteFlagOff {
        put(controller, "/e1/me/track_reposts/100").status ==== Status.Ok
      }
    }

    "DELETE /e1/me/track_reposts/:id" >> {
      "falls back to mothership" in new WriteFlagOff {
        delete(controller, "/e1/me/track_reposts/100").status ==== Status.Ok
      }
    }

    "PUT /e1/me/playlist_reposts/:id" >> {
      "falls back to mothership" in new WriteFlagOff {
        put(controller, "/e1/me/playlist_reposts/100").status ==== Status.Ok
      }
    }

    "DELETE /e1/me/playlist_reposts/:id" >> {
      "falls back to mothership" in new WriteFlagOff {
        delete(controller, "/e1/me/playlist_reposts/100").status ==== Status.Ok
      }
    }
  }

  "when write flag is turned on" >> {
    trait WriteFlagOn extends Context {
      override def writeToReposts(): Boolean = true
    }

    "PUT /e1/me/track_reposts/:id" >> {
      trait CreateTrackContext extends WriteFlagOn {
        def result: Result

        repostsClient
          .createRepost(session, track, baseUrl)
          .returns(Future.value(result))

        lazy val response = put(controller, "/e1/me/track_reposts/100", Map(), requestHeaders)
      }

      "when creating succeeds" in new CreateTrackContext {
        override def result = Created
        response.status ==== Status.Created
        response.body.length ==== 0
      }

      "when creating fails because the track does not exist" in new CreateTrackContext {
        override def result = NotFound
        response.status ==== Status.NotFound
        response.body.length ==== 0
      }

      "when creating fails because the track was already reposted" in new CreateTrackContext {
        override def result = AlreadyExists
        response.status ==== Status.Ok
        response.body.length ==== 0
      }

      "when creating fails because the user was blocked for spam" in new CreateTrackContext {
        override def result = SpamBlocked(Seq(SpamWarning("foo", "bar", Some("baz"), Some("fuz"))))
        response.status ==== Status.TooManyRequests
        response.jsonBody.as[SpamBlocked] ==== result
      }

      "when creating fails because of an unknown reason" in new CreateTrackContext {
        override def result = Failed
        response.status ==== Status.InternalServerError
        response.body.length ==== 0
      }
    }

    "DELETE /e1/me/track_reposts/:id" >> {
      trait DeleteTrackContext extends WriteFlagOn {
        def result: Result

        repostsClient
          .deleteRepost(session, track, baseUrl)
          .returns(Future.value(result))

        lazy val response = delete(controller, "/e1/me/track_reposts/100", Map(), requestHeaders)
      }

      "when deleting succeeds" in new DeleteTrackContext {
        override def result = Deleted
        response.status ==== Status.Ok
        response.body.length ==== 0
      }

      "when deleting fails because the track/repost does not exist" in new DeleteTrackContext {
        override def result = NotFound
        response.status ==== Status.NotFound
        response.body.length ==== 0
      }

      "when deleting fails because of an unknown reason" in new DeleteTrackContext {
        override def result = Failed
        response.status ==== Status.InternalServerError
        response.body.length ==== 0
      }
    }

    "PUT /e1/me/playlist_reposts/:id" >> {
      trait CreatePlaylistContext extends WriteFlagOn {
        def result: Result

        repostsClient
          .createRepost(session, playlist, baseUrl)
          .returns(Future.value(result))

        lazy val response = put(controller, "/e1/me/playlist_reposts/200", Map(), requestHeaders)
      }

      "when creating succeeds" in new CreatePlaylistContext {
        override def result = Created
        response.status ==== Status.Created
        response.body.length ==== 0
      }

      "when creating fails because the playlist does not exist" in new CreatePlaylistContext {
        override def result = NotFound
        response.status ==== Status.NotFound
        response.body.length ==== 0
      }

      "when creating fails because the playlist was already reposted" in new CreatePlaylistContext {
        override def result = AlreadyExists
        response.status ==== Status.Ok
        response.body.length ==== 0
      }

      "when creating fails because the user was blocked for spam" in new CreatePlaylistContext {
        override def result = SpamBlocked(Seq(SpamWarning("foo", "bar", Some("baz"), Some("fuz"))))
        response.status ==== Status.TooManyRequests
        response.jsonBody.as[SpamBlocked] ==== result
      }

      "when creating fails because of an unknown reason" in new CreatePlaylistContext {
        override def result = Failed
        response.status ==== Status.InternalServerError
        response.body.length ==== 0
      }
    }

    "DELETE /e1/me/playlist_reposts/:id" >> {
      trait DeletePlaylistContext extends WriteFlagOn {
        def result: Result

        repostsClient
          .deleteRepost(session, playlist, baseUrl)
          .returns(Future.value(result))

        lazy val response = delete(controller, "/e1/me/playlist_reposts/200", Map(), requestHeaders)
      }

      "when deleting succeeds" in new DeletePlaylistContext {
        override def result = Deleted
        response.status ==== Status.Ok
        response.body.length ==== 0
      }

      "when deleting fails because the playlist/repost does not exist" in new DeletePlaylistContext {
        override def result = NotFound
        response.status ==== Status.NotFound
        response.body.length ==== 0
      }

      "when deleting fails because of an unknown reason" in new DeletePlaylistContext {
        override def result = Failed
        response.status ==== Status.InternalServerError
        response.body.length ==== 0
      }
    }
  }
}
