package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.{Created, Deleted}
import com.twitter.util.Await

class RepostsClientSuccessSpec extends RepostsClientSpec {

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    interactions = List(
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:1 was not reposted by soundcloud:users:1"),
        request = repostTrackRequest,
        response = buildResponse(status = 201)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlist:1 was not reposted by soundcloud:users:1"),
        request = repostPlaylistRequest,
        response = buildResponse(status = 201)
      ),
      buildInteraction(
        description = "Deleting a track repost",
        maybeState = Some("soundcloud:tracks:1 was reposted by soundcloud:users:1"),
        request = unrepostTrackRequest,
        response = buildResponse(status = 202)
      ),
      buildInteraction(
        description = "Deleting a playlist repost",
        maybeState = Some("soundcloud:playlists:1 was reposted by soundcloud:users:1"),
        request = unrepostPlaylistRequest,
        response = buildResponse(status = 202)
      )
    )
  )

  pactFragment.description >> {
    "Creating a track repost" >> {
      "when reposting succeeds" in new Context {
        val result = Await.result(client.createRepost(session, track, baseUrl))
        result ==== Created
      }
    }

    "Creating a playlist repost" >> {
      "when reposting succeeds" in new Context {
        val result = Await.result(client.createRepost(session, playlist, baseUrl))
        result ==== Created
      }
    }

    "Deleting a track repost" >> {
      "when deleting succeeds" in new Context {
        val result = Await.result(client.deleteRepost(session, track, baseUrl))
        result ==== Deleted
      }
    }

    "Deleting a playlist repost" >> {
      "when deleting succeeds" in new Context {
        val result = Await.result(client.deleteRepost(session, playlist, baseUrl))
        result ==== Deleted
      }
    }
  }
}
