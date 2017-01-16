package com.soundcloud.publicApiStrangler.client.reposts

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.{PactSpec, UnitSpecsSupport}
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.{ResourceName, Urn, UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.twitter.finagle.http.HeaderMap
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.Json

class RepostsClientSpec extends UnitSpecification with PactSpec with UnitSpecsSupport {

  override val consumer = "public-api-strangler"
  override val provider = "reposts"

  private val headers = Map("content-type" -> "application/json;charset=utf-8")

  private val user = Urn(s"soundcloud:users:1")
  private val spamUser = Urn(s"soundcloud:users:2")

  private val spamResponse = buildResponse(
    status = 429,
    headers = Map("content-type" -> "application/json;charset=utf-8"),
    bodyAndMatchers = new PactDslJsonBody()
      .minArrayLike("spam_warnings", 1, 1)
      .stringType("level")
      .booleanType("acknowledgeable", false)
      .closeObject()
      .closeArray()
  )

  private def createRequest(urn: Urn, user: Urn) =
    buildRequest(
      path = s"/${urn.getCollection}/${urn.toString}/reposts",
      method = "POST",
      headers = Map("Sc-User" -> user.toString)
    )

  private def deleteRequest(urn: Urn, user: Urn) =
    buildRequest(
      path = s"/${urn.getCollection}/${urn.toString}/reposts",
      method = "DELETE",
      headers = Map("Sc-User" -> user.toString)
    )

  val countsInteractions = List(
    buildInteraction(
      description = "Get count of user's track reposts",
      maybeState = None,
      request = buildRequest(path = s"/users/$user/track_reposts/count", method = "GET"),
      response = buildResponse(
        status = 200,
        headers = headers,
        bodyAndMatchers = new PactDslJsonBody()
          .minArrayLike("counts", 1)
            .stringMatcher("urn", "soundcloud:[a-z0-9-]+:[a-z0-9-]+", user.toString)
            .integerType("count", 12L)
            .closeObject()
          .closeArray()
      )
    ),
    buildInteraction(
      description = "Get count of user's playlist reposts",
      maybeState = None,
      request = buildRequest(path = s"/users/$user/playlist_reposts/count", method = "GET"),
      response = buildResponse(
        status = 200,
        headers = headers,
        bodyAndMatchers = new PactDslJsonBody()
          .minArrayLike("counts", 1)
            .stringMatcher("urn", "soundcloud:[a-z0-9-]+:[a-z0-9-]+", user.toString)
            .integerType("count", 13L)
            .closeObject()
          .closeArray()
      )
    ),
    buildInteraction(
      description = "Get counts of reposts for tracks",
      maybeState = None,
      request = buildRequest(
        path = s"/tracks/reposts/count",
        method = "GET",
        query = "urns=soundcloud:tracks:1,soundcloud:tracks:2"
      ),
      response = buildResponse(
        status = 200,
        headers = headers,
        maybeBody = Some(Json.obj("counts" -> Json.arr(
          Json.obj("urn" -> "soundcloud:tracks:1", "count" -> 12L),
          Json.obj("urn" -> "soundcloud:tracks:2", "count" -> 13L)
        )).toString),
        matchers = Map(
          "$.body.counts[*].count" -> Map("match" -> "integer"),
          "$.body.counts[*].urn" -> Map("regex" -> "soundcloud:[a-z0-9-]+:[a-z0-9-]+"),
          "$.body.counts" -> Map(
            "min" -> "2",
            "match" -> "type"
          )
        )
      )
    ),
    buildInteraction(
      description = "Get counts of reposts for playlists",
      maybeState = None,
      request = buildRequest(
        path = s"/playlists/reposts/count",
        method = "GET",
        query = "urns=soundcloud:playlists:1,soundcloud:playlists:2"
      ),
      response = buildResponse(
        status = 200,
        headers = headers,
        maybeBody = Some(Json.obj("counts" -> Json.arr(
          Json.obj("urn" -> "soundcloud:playlists:1", "count" -> 14L),
          Json.obj("urn" -> "soundcloud:playlists:2", "count" -> 15L)
        )).toString),
        matchers = Map(
          "$.body.counts[*].count" -> Map("match" -> "integer"),
          "$.body.counts[*].urn" -> Map("regex" -> "soundcloud:[a-z0-9-]+:[a-z0-9-]+"),
          "$.body.counts" -> Map(
            "min" -> "2",
            "match" -> "type"
          )
        )
      )
    )
  )

  val trackRepostInteractions = List(
    buildInteraction(
      description = "Creating a track repost",
      maybeState = Some("soundcloud:tracks:1 was not reposted by soundcloud:users:1"),
      request = createRequest(Urn("soundcloud:tracks:1"), user),
      response = buildResponse(status = 201)
    ),
    buildInteraction(
      description = "Deleting a track repost",
      maybeState = Some("soundcloud:tracks:1 was reposted by soundcloud:users:1"),
      request = deleteRequest(Urn("soundcloud:tracks:1"), user),
      response = buildResponse(status = 202)
    ),
    buildInteraction(
      description = "Creating a track repost",
      maybeState = Some("soundcloud:tracks:2 does not exist"),
      request = createRequest(Urn("soundcloud:tracks:2"), user),
      response = buildResponse(status = 404)
    ),
    buildInteraction(
      description = "Creating a track repost",
      maybeState = Some("soundcloud:tracks:3 was reposted by soundcloud:users:1"),
      request = createRequest(Urn("soundcloud:tracks:3"), user),
      response = buildResponse(status = 200)
    ),
    buildInteraction(
      description = "Deleting a track repost",
      maybeState = Some("soundcloud:tracks:3 was not reposted by soundcloud:users:1"),
      request = deleteRequest(Urn("soundcloud:tracks:3"), user),
      response = buildResponse(status = 404)
    ),
    buildInteraction(
      description = "Creating a track repost",
      maybeState = Some("soundcloud:users:2 is blocked for spam"),
      request = createRequest(Urn("soundcloud:tracks:4"), spamUser),
      response = spamResponse
    )
  )

  val playlistRepostInteractions = List(
    buildInteraction(
      description = "Creating a playlist repost",
      maybeState = Some("soundcloud:playlists:1 was not reposted by soundcloud:users:1"),
      request = createRequest(Urn("soundcloud:playlists:1"), user),
      response = buildResponse(status = 201)
    ),
    buildInteraction(
      description = "Deleting a playlist repost",
      maybeState = Some("soundcloud:playlists:1 was reposted by soundcloud:users:1"),
      request = deleteRequest(Urn("soundcloud:playlists:1"), user),
      response = buildResponse(status = 202)
    ),
    buildInteraction(
      description = "Creating a playlist repost",
      maybeState = Some("soundcloud:playlists:2 does not exist"),
      request = createRequest(Urn("soundcloud:playlists:2"), user),
      response = buildResponse(status = 404)
    ),
    buildInteraction(
      description = "Creating a playlist repost",
      maybeState = Some("soundcloud:playlists:3 was reposted by soundcloud:users:1"),
      request = createRequest(Urn("soundcloud:playlists:3"), user),
      response = buildResponse(status = 200)
    ),
    buildInteraction(
      description = "Deleting a playlist repost",
      maybeState = Some("soundcloud:playlists:3 was not reposted by soundcloud:users:1"),
      request = deleteRequest(Urn("soundcloud:playlists:3"), user),
      response = buildResponse(status = 404)
    ),
    buildInteraction(
      description = "Creating a playlist repost",
      maybeState = Some("soundcloud:users:2 is blocked for spam"),
      request = createRequest(Urn("soundcloud:playlists:4"), spamUser),
      response = spamResponse
    )
  )

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    trackRepostInteractions ++ playlistRepostInteractions ++ countsInteractions
  )

  pactFragment.description >> {
    trait Context extends Scope {
      def sessionUser: Urn
      lazy val session = new UserSessionBuilder().setUser(sessionUser).build()
      val baseUrl = "http://api.example.com"

      val client: RepostsClient = {
        val config = new InMemoryConfig
        config.set("APP_NAME", consumer)
        config.set(s"${provider.toUpperCase}_JSONCLIENT_REQUEST_TIMEOUT_MILLIS", "5000")

        val jsonClient = JsonClient(
          ResourceName(provider),
          ServiceEntryPoint(providerConfig.url),
          config,
          new Telemetry(config)
        )
        new RepostsClient(jsonClient)
      }
    }

    "when requests are successful" >> {
      trait SuccessfulContext extends Context {
        val sessionUser = user
        val track = Urn("soundcloud:tracks:1")
        val playlist = Urn("soundcloud:playlists:1")
      }

      "Creating a track repost" in new SuccessfulContext {
        Await.result(client.createRepost(session, track, baseUrl)) ==== Created
      }

      "Creating a playlist repost" in new SuccessfulContext {
        Await.result(client.createRepost(session, playlist, baseUrl)) ==== Created
      }

      "Deleting a track repost" in new SuccessfulContext {
        Await.result(client.deleteRepost(session, track, baseUrl)) ==== Deleted
      }

      "Deleting a playlist repost" in new SuccessfulContext {
        Await.result(client.deleteRepost(session, playlist, baseUrl)) ==== Deleted
      }
    }

    "when the target does not exist" >> {
      trait NotFoundContext extends Context {
        val sessionUser = user
        val track = Urn("soundcloud:tracks:2")
        val playlist = Urn("soundcloud:playlists:2")
      }

      "Creating a track repost" in new NotFoundContext {
        Await.result(client.createRepost(session, track, baseUrl)) ==== NotFound
      }

      "Creating a playlist repost" in new NotFoundContext {
        Await.result(client.createRepost(session, playlist, baseUrl)) ==== NotFound
      }
    }

    "when the target was already reposted" >> {
      trait AlreadyRepostedContext extends Context {
        val sessionUser = user
        val track = Urn("soundcloud:tracks:3")
        val playlist = Urn("soundcloud:playlists:3")
      }

      "Creating a track repost" in new AlreadyRepostedContext {
        Await.result(client.createRepost(session, track, baseUrl)) ==== AlreadyExists
      }

      "Creating a playlist repost" in new AlreadyRepostedContext {
        Await.result(client.createRepost(session, playlist, baseUrl)) ==== AlreadyExists
      }
    }

    "when the target was not reposted" >> {
      trait NotRepostedContext extends Context {
        val sessionUser = user
        val track = Urn("soundcloud:tracks:3")
        val playlist = Urn("soundcloud:playlists:3")
      }

      "Deleting a track repost" in new NotRepostedContext {
        Await.result(client.deleteRepost(session, track, baseUrl)) ==== NotFound
      }

      "Deleting a playlist repost" in new NotRepostedContext {
        Await.result(client.deleteRepost(session, playlist, baseUrl)) ==== NotFound
      }
    }

    "when the user is blocked for spam" >> {
      trait SpamContext extends Context {
        val sessionUser = spamUser
        val track = Urn("soundcloud:tracks:4")
        val playlist = Urn("soundcloud:playlists:4")
      }

      "Creating a track repost" in new SpamContext {
        val result = Await.result(client.createRepost(session, track, baseUrl))
        result match {
          case SpamBlocked(errors) => errors should not be empty
          case other => failure(s"Expected SpamBlocked, instead got $other")
        }
      }

      "Creating a playlist repost" in new SpamContext {
        val result = Await.result(client.createRepost(session, playlist, baseUrl))
        result match {
          case SpamBlocked(errors) => errors should not be empty
          case other => failure(s"Expected SpamBlocked, instead got $other")
        }
      }
    }

    "#getRepostCountsByUrnWithFallback" >> {
      trait CountsContext extends Context {
        val sessionUser = user
        val track1 = Urn("soundcloud:tracks:1")
        val track2 = Urn("soundcloud:tracks:2")
        val playlist1 = Urn("soundcloud:playlists:1")
        val playlist2 = Urn("soundcloud:playlists:2")
      }

      "when data is available" >> {
        "it returns total reposts by a user" in new CountsContext {
          Await.result(client.getRepostCountsByUrnWithFallback(session, Set(user))) ==== Map(user -> 25L)
        }
        "it returns repost counts of different types" in new CountsContext {
          val urns: Set[Urn] = collection.immutable.TreeSet(user, track1, track2, playlist1, playlist2)
          val f = client.getRepostCountsByUrnWithFallback(session, urns)
          Await.result(f) ==== Map(user -> 25L, track1 -> 12L, track2 -> 13L, playlist1 -> 14L, playlist2 -> 15L)
        }
      }

      trait MockedJsonClient extends Context {
        val jsonClientMock = mock[JsonClient]
        val clientWithMock = new RepostsClient(jsonClientMock)
      }

      "when data is not available, but the calls were successful" >> {
        "it falls back to zero counts for unavailable URNs" in new CountsContext with MockedJsonClient {
          when(jsonClientMock.get(any[UserSession], any[Path], any[Params], any[Params])) thenReturn
            Future.value(JsonResponse(OkStatus, Json.obj("counts" -> Json.arr()), HeaderMap(("content-type", "application/json;charset=utf-8"))))

          val f = clientWithMock.getRepostCountsByUrnWithFallback(session, Set(user, track1, track2, playlist1, playlist2))
          Await.result(f) ==== Map(user -> 0L, track1 -> 0L, track2 -> 0L, playlist1 -> 0L, playlist2 -> 0L)
        }
      }

      "when an upstream request fails call fails" >> {
        "it falls back to zero counts for unavailable URNs" in new CountsContext with MockedJsonClient {
          when(jsonClientMock.get(any[UserSession], any[Path], any[Params], any[Params])).thenReturn(Future.???)
          val f = clientWithMock.getRepostCountsByUrnWithFallback(session, Set(user, track1, track2, playlist1, playlist2))

          Await.result(f) ==== Map(user -> 0L, track1 -> 0L, track2 -> 0L, playlist1 -> 0L, playlist2 -> 0L)
        }
      }
    }

  }
}
