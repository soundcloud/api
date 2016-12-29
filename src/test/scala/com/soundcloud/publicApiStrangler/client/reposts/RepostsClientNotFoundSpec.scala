package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.NotFound
import com.twitter.util.Await

class RepostsClientNotFoundSpec extends RepostsClientSpec {

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    interactions = List(
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:1 does not exist"),
        request = repostTrackRequest,
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlists:1 does not exist"),
        request = repostPlaylistRequest,
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Deleting a track repost",
        maybeState = Some("soundcloud:tracks:1 was not reposted by soundcloud:users:1"),
        request = unrepostTrackRequest,
        response = buildResponse(status = 404)
      ),
      buildInteraction(
        description = "Deleting a playlist repost",
        maybeState = Some("soundcloud:playlists:1 was not reposted by soundcloud:users:1"),
        request = unrepostPlaylistRequest,
        response = buildResponse(status = 404)
      )
    )
  )

  pactFragment.description >> {
    "Creating a track repost" >> {
      "when the track does not exist" in new Context {
        val result = Await.result(client.createRepost(session, track, baseUrl))
        result ==== NotFound
      }
    }

    "Creating a playlist repost" >> {
      "when the playlist does not exist" in new Context {
        val result = Await.result(client.createRepost(session, playlist, baseUrl))
        result ==== NotFound
      }
    }

    "Deleting a track repost" >> {
      "when the track was not reposted" in new Context {
        val result = Await.result(client.deleteRepost(session, track, baseUrl))
        result ==== NotFound
      }
    }

    "Deleting a playlist repost" >> {
      "when the playlist was not reposted" in new Context {
        val result = Await.result(client.deleteRepost(session, playlist, baseUrl))
        result ==== NotFound
      }
    }
  }
}
