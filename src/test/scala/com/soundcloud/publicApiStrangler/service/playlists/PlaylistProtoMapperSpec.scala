package com.soundcloud.publicApiStrangler.service.playlists

import java.time.Instant

import com.soundcloud.jvmkit.module.twirp.proto.WellKnownOps._
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification
import proto.soundcloud.playlists.api.{PlaylistResponse, Playlist => ProtoPlaylist}

class PlaylistProtoMapperSpec extends Specification {
  trait Context extends Scope {
    val defaultInstant = Instant.now()
    val defaultProtoDate = defaultInstant.asProto

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
      likesCount = 500,
      isAlbum = false,
      setType = "set",
      managedByFeeds = false,
      repostsCount = 50,
      publishedAt = Some(defaultProtoDate),
      embeddableBy = "all",
      license = Some("gtp"),
      labelName = Some("red eye records"),
      labelId = Some("123"),
      purchaseTitle = Some("purchase title"),
      purchaseUrl = Some("http://foo.bar.com"),
      cursor = Some("cursor"),
      ean = Some("ean"),
      streamable = true,
      uri = "uri"
    )
  }

  "createPlaylist" >> {
    "can create VisiblePlaylist" in new Context {
      val mapper = new PlaylistProtoMapper
      val result = mapper(PlaylistResponse(playlist = Some(playlistProto)))
      result must not beEmpty

      val visiblePlaylist = result.get
      visiblePlaylist.urn ==== "soundcloud:playlists:123"
      visiblePlaylist.title ==== "my favourite music"
      visiblePlaylist.description ==== Some("bla bla bla")
      visiblePlaylist.createdAt ==== Some(defaultInstant)
      visiblePlaylist.duration ==== 1111
      visiblePlaylist.genre ==== "music?"
      visiblePlaylist.permalink ==== "my_favourite_music"
      visiblePlaylist.permalinkUrl ==== "http://api.soundcloud.com/my_favourite_music"
      visiblePlaylist.artworkUrl ==== "http://foo.com/artwork"
      visiblePlaylist.trackCount ==== 5
      visiblePlaylist.releaseDate ==== Some(defaultInstant)
      visiblePlaylist.public ==== true
      visiblePlaylist.sharing ==== "public"
      visiblePlaylist.secretToken ==== Some("s3creT")
      visiblePlaylist.updatedAt ==== Some(defaultInstant)
      visiblePlaylist.userUrn ==== "soundcloud:users:1"
      visiblePlaylist.likesCount ==== 500
      visiblePlaylist.isAlbum ==== false
      visiblePlaylist.setType ==== "set"
      visiblePlaylist.managedByFeeds ==== false
      visiblePlaylist.repostsCount ==== 50
      visiblePlaylist.publishedAt ==== Some(defaultInstant)
      visiblePlaylist.embeddableBy ==== "all"
      visiblePlaylist.license ==== Some("gtp")
      visiblePlaylist.labelName ==== Some("red eye records")
      visiblePlaylist.labelId ==== Some("123")
      visiblePlaylist.purchaseTitle ==== Some("purchase title")
      visiblePlaylist.purchaseUrl ==== Some("http://foo.bar.com")
      visiblePlaylist.cursor ==== Some("cursor")
      visiblePlaylist.ean ==== Some("ean")
      visiblePlaylist.streamable ==== true
      visiblePlaylist.uri ==== "uri"
    }

    "returns None when no visible playlist supplied" in new Context {
      val mapper = new PlaylistProtoMapper
      val result = mapper(PlaylistResponse(playlist = None))
      result must beEmpty
    }
  }
}
