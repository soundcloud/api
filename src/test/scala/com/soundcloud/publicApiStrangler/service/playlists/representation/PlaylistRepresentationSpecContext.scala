package com.soundcloud.publicApiStrangler.service.playlists.representation

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.User

trait PlaylistRepresentationSpecContext {
  val user =
    User(
      urn = Urn("soundcloud", "users", "1"),
      permalink = "giraffe",
      username = "Dr. G. Raffe",
      avatar_url = "http://example.com/giraffe.jpg?123456789",
      permalink_url = "https://soundcloud.com/denis",
      city = None,
      country = None,
      tracks_count = 1,
      followers_count = Some(20000),
      followings_count = Some(20),
      verified = false,
      description = Some("I am a nice person"),
      updated_at = Some("2016/10/10 11:21:36 +0000")
    )

  val playlist =
    Playlist(
      title = "test",
      id = 1,
      duration = 120,
      userId = 1,
      kind = "playlist",
      releaseDay = None,
      permalinkUrl = "http://soundcloud.com/test",
      genre = "metal",
      permalink = "test",
      purchaseUrl = None,
      releaseMonth = None,
      description = None,
      uri = "http://soundcloud.com",
      labelName = None,
      label = None,
      tagList = "",
      releaseYear = None,
      trackCount = 1,
      lastModified = None,
      license = None,
      playlistType = "",
      downloadable = None,
      sharing = "",
      createdAt = None,
      release = None,
      purchaseTitle = None,
      artworkUrl = "",
      ean = None,
      streamable = Some(false),
      embeddableBy = "",
      labelId = None,
      user = user,
      tracks = List.empty,
      secretToken = None,
      secretUri = None
    )
}
