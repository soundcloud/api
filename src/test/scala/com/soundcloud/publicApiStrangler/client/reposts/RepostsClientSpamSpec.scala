package com.soundcloud.publicApiStrangler.client.reposts

import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.SpamBlocked
import com.twitter.util.Await

class RepostsClientSpamSpec extends RepostsClientSpec {

  override def user = Urn(s"soundcloud:users:2")

  val response = buildResponse(
    status = 429,
    headers = ResponseHeaders,
    bodyAndMatchers = new PactDslJsonBody()
      .minArrayLike("spam_warnings", 1, 1)
      .stringType("level")
      .booleanType("acknowledgeable", false)
      .closeObject()
      .closeArray()
  )

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    interactions = List(
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:users:2 is blocked for spam"),
        request = repostTrackRequest,
        response = response
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:users:2 is blocked for spam"),
        request = repostPlaylistRequest,
        response = response
      )
    )
  )

  pactFragment.description >> {
    "Creating a track repost" >> {
      "when the user is spam blocked" in new Context {
        val result = Await.result(client.createRepost(session, track, baseUrl))
        result match {
          case SpamBlocked(errors) => errors should not be empty
          case other => failure(s"Expected SpamBlock, instead got $other")
        }
      }
    }

    "Creating a playlist repost" >> {
      "when the user is spam blocked" in new Context {
        val result = Await.result(client.createRepost(session, playlist, baseUrl))
        result match {
          case SpamBlocked(errors) => errors should not be empty
          case other => failure(s"Expected SpamBlock, instead got $other")
        }
      }
    }
  }
}
