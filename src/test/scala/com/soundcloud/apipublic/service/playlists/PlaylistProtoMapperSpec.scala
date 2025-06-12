package com.soundcloud.apipublic.service.playlists

import java.time.Instant
import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.client.tracks.TrackRequest
import com.soundcloud.apipublic.service.pagination.OffsetBasedPagination
import com.soundcloud.apipublic.service.playlists.representation.PlaylistCounts
import com.twitter.finagle.http.ParamMap
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification
import proto.soundcloud.playlists.api.{
  PlaylistPagination,
  PlaylistResponse,
  Counts => ProtoCounts,
  Playlist => ProtoPlaylist,
  TrackRequest => ProtoTrackRequest
}

class PlaylistProtoMapperSpec extends Specification {
  trait Context extends Scope {
    val defaultInstant = Instant.now()
    val defaultProtoDate = defaultInstant.asProto

    val trackUrn = Urn("soundcloud", "tracks", "1")
    val trackSecret = Some("s3creT")
    val protoTrackRequests = Seq(ProtoTrackRequest(urn = trackUrn.toString, secretToken = trackSecret))

    val currentPagination = Some(
      OffsetBasedPagination(
        baseUrl = "http://api.soundcloud.com",
        path = "/playlists",
        extraParams = ParamMap(),
        offset = None,
        limit = 1
      )
    )
    val nextPagination = Some(PlaylistPagination(limit = 1, cursor = Some("2")))

    val playlistProto = new ProtoPlaylist(
      urn = "soundcloud:playlists:123",
      title = "my favourite music",
      description = Some("bla bla bla"),
      createdAt = Some(defaultProtoDate),
      duration = 1111,
      genre = "music?",
      permalink = "my_favourite_music",
      permalinkUrl = "http://api.soundcloud.com/my_favourite_music",
      artworkUrl = "http://foo.com/artwork",
      trackCount = 5,
      userTags = Seq("music"),
      releaseDate = Some(defaultProtoDate),
      public = true,
      sharing = "public",
      secretToken = Some("s3creT"),
      updatedAt = Some(defaultProtoDate),
      userUrn = "soundcloud:users:1",
      isAlbum = false,
      setType = "set",
      managedByFeeds = false,
      publishedAt = Some(defaultProtoDate),
      embeddableBy = "all",
      license = Some("gtp"),
      labelName = Some("red eye records"),
      labelId = Some("123"),
      purchaseTitle = Some("purchase title"),
      purchaseUrl = Some("http://foo.bar.com"),
      ean = Some("ean"),
      streamable = Some(true),
      uri = "https://api.soundcloud.com/playlists/soundcloud:playlists:123",
      counts = Some(ProtoCounts(Some(2)))
    )
  }

  "createPlaylist" >> {
    "can create VisiblePlaylist" in new Context {
      val mapper = new PlaylistProtoMapper
      val result = mapper(
        PlaylistResponse(
          playlist = Some(playlistProto),
          trackRequests = protoTrackRequests,
          pagination = nextPagination
        ),
        currentPagination
      )

      result must not beEmpty

      val expectedPagination = currentPagination.map(pagination => pagination.copy(offset = Some(2)))
      val expectedTrackRequests = List(TrackRequest(urn = trackUrn, secretToken = trackSecret))

      val visiblePlaylist = result.get

      visiblePlaylist.trackRequests.requests ==== expectedTrackRequests
      visiblePlaylist.trackRequests.pagination ==== expectedPagination

      visiblePlaylist.urn ==== "soundcloud:playlists:123"
      visiblePlaylist.title ==== "my favourite music"
      visiblePlaylist.description ==== Some("bla bla bla")
      visiblePlaylist.createdAt ==== Some(defaultInstant)
      visiblePlaylist.duration ==== 1111
      visiblePlaylist.genre ==== "music?"
      visiblePlaylist.permalink ==== "my_favourite_music"
      visiblePlaylist.permalinkUrl ==== "http://api.soundcloud.com/my_favourite_music"
      visiblePlaylist.artworkUrl ==== Some("http://foo.com/artwork")
      visiblePlaylist.trackCount ==== 5
      visiblePlaylist.releaseDate ==== Some(defaultInstant)
      visiblePlaylist.public ==== true
      visiblePlaylist.sharing ==== "public"
      visiblePlaylist.secretToken ==== Some("s3creT")
      visiblePlaylist.updatedAt ==== Some(defaultInstant)
      visiblePlaylist.userUrn ==== "soundcloud:users:1"
      visiblePlaylist.isAlbum ==== false
      visiblePlaylist.setType ==== "set"
      visiblePlaylist.managedByFeeds ==== false
      visiblePlaylist.publishedAt ==== Some(defaultInstant)
      visiblePlaylist.embeddableBy ==== "all"
      visiblePlaylist.license ==== Some("gtp")
      visiblePlaylist.labelName ==== Some("red eye records")
      visiblePlaylist.labelId ==== Some("123")
      visiblePlaylist.purchaseTitle ==== Some("purchase title")
      visiblePlaylist.purchaseUrl ==== Some("http://foo.bar.com")
      visiblePlaylist.ean ==== Some("ean")
      visiblePlaylist.streamable ==== Some(true)
      visiblePlaylist.uri ==== "https://api.soundcloud.com/playlists/soundcloud:playlists:123"
      visiblePlaylist.counts.get ==== PlaylistCounts(Some(2))
    }

    "returns None when no visible playlist supplied" in new Context {
      val mapper = new PlaylistProtoMapper
      val result = mapper(PlaylistResponse(playlist = None), pagination = None)
      result must beEmpty
    }
  }
}
