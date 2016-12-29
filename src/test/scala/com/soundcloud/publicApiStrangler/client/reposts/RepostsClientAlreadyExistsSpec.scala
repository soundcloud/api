package com.soundcloud.publicApiStrangler.client.reposts

import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient.AlreadyExists
import com.twitter.util.Await

class RepostsClientAlreadyExistsSpec extends RepostsClientSpec {

  override val pactFragment = buildPactFragment(
    consumer = consumer,
    provider = provider,
    interactions = List(
      buildInteraction(
        description = "Creating a track repost",
        maybeState = Some("soundcloud:tracks:1 was already reposted by soundcloud:users:1"),
        request = repostTrackRequest,
        response = buildResponse(status = 200)
      ),
      buildInteraction(
        description = "Creating a playlist repost",
        maybeState = Some("soundcloud:playlists:1 was already reposted by soundcloud:users:1"),
        request = repostPlaylistRequest,
        response = buildResponse(status = 200)
      )
    )
  )

  pactFragment.description >> {
    "Creating a track repost" >> {
      "when the track was already reposted" in new Context {
        val result = Await.result(client.createRepost(session, track, baseUrl))
        result ==== AlreadyExists
      }
    }

    "Creating a playlist repost" >> {
      "when the playlist was already reposted" in new Context {
        val result = Await.result(client.createRepost(session, playlist, baseUrl))
        result ==== AlreadyExists
      }
    }
  }
}
