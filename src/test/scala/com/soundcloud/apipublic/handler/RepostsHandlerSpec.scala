package com.soundcloud.apipublic.handler

import com.soundcloud.jvmkit.module.bff.testsupport.FakeUserAuthentication
import com.soundcloud.jvmkit.module.outcome.{Bad, GoodOps, Outcome}
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.reposts.RepostsClient._
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.RepostsService
import com.soundcloud.apipublic.service.pagination.CursorBasedPagination
import com.soundcloud.apipublic.service.playlists.PlaylistBuilder
import com.soundcloud.apipublic.service.representation.collection.Collection
import com.soundcloud.apipublic.service.trackrepresentation.TrackRepresentationSpecContext
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.twitter.finagle.http.{ParamMap, Status}
import com.twitter.util.Future

class RepostsHandlerSpec extends UnitSpecification with TrackRepresentationSpecContext {
  trait Context extends HandlerSpecificationScope {
    val userUrn = Urn("soundcloud", "users", "999")
    val user = new UserBuilder().setUrn(userUrn).build
    val userCollection = Collection[UserRepresentation](items = List(user), nextHref = None)

    val track = Urn("soundcloud", "tracks", "100")
    val playlist = Urn("soundcloud", "playlists", "200")
    val geo = new Geo("US")
    val requestHeaders = Map[String, String]()
    val baseUrl = "https://api.example.com"
    val session =
      new UserSessionBuilder().setUser(userUrn).setAgent(Urn("soundcloud", "applications", "v2")).setGeo(geo).build()
    val repostsService = mock[RepostsService]

    lazy val handler = new RepostsHandler(new FakeUserAuthentication(session), repostsService, baseUrl)

    override def routingDefinitions = Routing.forRepostsHandler(handler)

    def trackRepresentation(urn: Urn) = createTrackRepresentationFromVisibleTrack().copy(urn = urn)
  }

  "POST /reposts/tracks/:id" >> {
    trait CreateTrackContext extends Context {
      def result: Result

      repostsService
        .createTracksRepost(session, track)
        .returns(Future.value(result))

      lazy val response = post("/reposts/tracks/100", Map(), requestHeaders)
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

  "GET /me/reposts/tracks" >> {
    "when retrieving succeeds" in new Context {
      val aTrackRep = trackRepresentation(Urn("soundcloud:tracks:1"))

      repostsService
        .getTrackReposts(any[UserSession], ===(userUrn), any[AccessParams], any[CursorBasedPagination])
        .returns(Future.value(Collection(List(aTrackRep), None).good))

      val response = get("/me/reposts/tracks", Map(), requestHeaders)

      response.status ==== Status.Ok
    }
  }

  "GET /users/:userId/reposts/tracks" >> {
    "when retrieving succeeds" in new Context {
      val aTrackRep = trackRepresentation(Urn("soundcloud:tracks:1"))

      repostsService
        .getTrackReposts(any[UserSession], ===(userUrn), any[AccessParams], any[CursorBasedPagination])
        .returns(Future.value(Collection(List(aTrackRep), None).good))

      val response = get(s"/users/${userUrn.identifier}/reposts/tracks", Map(), requestHeaders)

      response.status ==== Status.Ok
    }
  }

  "GET /me/reposts/playlists" >> {
    "when retrieving succeeds" in new Context {
      val aPlaylist = new PlaylistBuilder().setUrn(Urn("soundcloud:playlists:1")).build
      repostsService
        .getPlaylistReposts(any[UserSession], ===(userUrn), any[CursorBasedPagination])
        .returns(Future.value(Collection(List(aPlaylist), None).good))

      val response = get("/me/reposts/playlists", Map(), requestHeaders)

      response.status ==== Status.Ok
    }
  }

  "GET /users/:userId/reposts/playlists" >> {
    "when retrieving succeeds" in new Context {
      val aPlaylist = new PlaylistBuilder().setUrn(Urn("soundcloud:playlists:1")).build
      repostsService
        .getPlaylistReposts(any[UserSession], ===(userUrn), any[CursorBasedPagination])
        .returns(Future.value(Collection(List(aPlaylist), None).good))

      val response = get(s"/users/${userUrn.identifier}/reposts/playlists", Map(), requestHeaders)

      response.status ==== Status.Ok
    }
  }

  "DELETE /reposts/tracks/:id" >> {
    trait DeleteTrackContext extends Context {
      def result: Result

      repostsService
        .deleteTracksRepost(session, track)
        .returns(Future.value(result))

      lazy val response = delete("/reposts/tracks/100", Map(), requestHeaders)
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

  "GET /tracks/:id/reposters" >> {
    trait GetTrackRepostersContext extends Context {
      def result: Outcome[Collection[UserRepresentation]]

      val pagination = CursorBasedPagination(
        "https://api.example.com",
        "/tracks/100/reposters",
        ParamMap(),
        None,
        1
      )

      repostsService
        .getTrackReposters(session, track, pagination)
        .returns(Future.value(result))

      lazy val response =
        get("/tracks/100/reposters?limit=1", Map(), requestHeaders)
    }

    "when getting succeeds" in new GetTrackRepostersContext {
      val expectedResponse = Collection.getRepresentation(result.right.get, true)
      override def result = userCollection.good

      response.status ==== Status.Ok
      response.contentString ==== expectedResponse
    }

    "when getting returns not found because the repost does not exist" in new GetTrackRepostersContext {
      override def result = Bad(com.soundcloud.jvmkit.module.outcome.NotFound())

      response.status ==== Status.NotFound
    }
  }

  "POST /reposts/playlists/:id" >> {
    trait CreatePlaylistContext extends Context {
      def result: Result

      repostsService
        .createPlaylistsRepost(session, playlist)
        .returns(Future.value(result))

      lazy val response = post("/reposts/playlists/200", Map(), requestHeaders)
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

  "DELETE /reposts/playlists/:id" >> {
    trait DeletePlaylistContext extends Context {
      def result: Result

      repostsService
        .deletePlaylistsRepost(session, playlist)
        .returns(Future.value(result))

      lazy val response = delete("/reposts/playlists/200", Map(), requestHeaders)
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

  "GET /playlists/:id/reposters" >> {
    trait GetPlaylistRepostersContext extends Context {
      def result: Collection[UserRepresentation]

      val expectedResponse = Collection.getRepresentation(result, true)
      val pagination = CursorBasedPagination(
        "https://api.example.com",
        "/playlists/200/reposters",
        ParamMap(),
        None,
        1
      )

      repostsService
        .getPlaylistReposters(session, playlist, pagination)
        .returns(Future.value(result))

      lazy val response =
        get("/playlists/200/reposters?limit=1", Map(), requestHeaders)
    }

    "when getting succeeds" in new GetPlaylistRepostersContext {
      override def result = userCollection

      response.status ==== Status.Ok
      response.contentString ==== expectedResponse
    }

    "when getting returns empty collection because the playlist/repost does not exist" in new GetPlaylistRepostersContext {
      override def result = Collection[UserRepresentation](List.empty, None)

      response.status ==== Status.Ok
      response.contentString ==== expectedResponse
    }
  }
}
