package com.soundcloud.publicApiStrangler.client.reposts

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.{PactSpec, UnitSpecsSupport}
import com.soundcloud.jvmkit.telemetry.Telemetry
import com.soundcloud.jvmkit.test.InMemoryConfig
import com.soundcloud.jvmkit.{ResourceName, Urn, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.PactHelper
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient._
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.twitter.util.Await
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification

class RepostsClientSpec extends Specification with PactSpec with UnitSpecsSupport with PactHelper {

  override val consumer = "public-api-strangler"
  override val provider = "reposts"

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

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    interactions = List(
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:1 was not reposted by soundcloud:users:1"),
        request = createRequest(Urn("soundcloud:tracks:1"), user),
        response = buildResponse(status = 201)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlists:1 was not reposted by soundcloud:users:1"),
        request = createRequest(Urn("soundcloud:playlists:1"), user),
        response = buildResponse(status = 201)
      ),
      buildInteraction(
        description = "Deleting a track repost",
        maybeState = Some("soundcloud:tracks:1 was reposted by soundcloud:users:1"),
        request = deleteRequest(Urn("soundcloud:tracks:1"), user),
        response = buildResponse(status = 202)
      ),
      buildInteraction(
        description = "Deleting a playlist repost",
        maybeState = Some("soundcloud:playlists:1 was reposted by soundcloud:users:1"),
        request = deleteRequest(Urn("soundcloud:playlists:1"), user),
        response = buildResponse(status = 202)
      ),
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:2 does not exist"),
        request = createRequest(Urn("soundcloud:tracks:2"), user),
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlists:2 does not exist"),
        request = createRequest(Urn("soundcloud:playlists:2"), user),
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:3 was reposted by soundcloud:users:1"),
        request = createRequest(Urn("soundcloud:tracks:3"), user),
        response = buildResponse(status = 200)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlists:3 was reposted by soundcloud:users:1"),
        request = createRequest(Urn("soundcloud:playlists:3"), user),
        response = buildResponse(status = 200)
      ),
      buildInteraction(
        description = "Deleting a track repost",
        maybeState = Some("soundcloud:tracks:3 was not reposted by soundcloud:users:1"),
        request = deleteRequest(Urn("soundcloud:tracks:3"), user),
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Deleting a playlist repost",
        maybeState = Some("soundcloud:playlists:3 was not reposted by soundcloud:users:1"),
        request = deleteRequest(Urn("soundcloud:playlists:3"), user),
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:users:2 is blocked for spam"),
        request = createRequest(Urn("soundcloud:tracks:4"), spamUser),
        response = spamResponse
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:users:2 is blocked for spam"),
        request = createRequest(Urn("soundcloud:playlists:4"), spamUser),
        response = spamResponse
      )
    )
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
  }
}
