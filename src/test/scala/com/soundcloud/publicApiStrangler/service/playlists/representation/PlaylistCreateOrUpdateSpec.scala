package com.soundcloud.publicApiStrangler.service.playlists.representation

import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{NullValue, Value}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import proto.soundcloud.playlists.api.{PlaylistCreateOrUpdate => ProtoPlaylistCreateOrUpdate, Tracks => ProtoTracks}

class PlaylistCreateOrUpdateSpec extends Specification {
  "correct maps values to proto" in new Scope {
    val playlistCreateOrUpdate = PlaylistCreateOrUpdate(
      description = Value("description"),
      ean = Value("ean"),
      genre = Value("genre"),
      label_name = Value("label name"),
      license = Value("license"),
      permalink = Value("permalink"),
      permalink_url = Value("permalink url"),
      public = Value(true),
      purchase_title = Value("purchase title"),
      release = Value("release"),
      release_date = Value("release date"),
      set_type = Value("set type"),
      tag_list = Value("tag list"),
      title = Value("title"),
      tracks = Value(Seq(Map("id" -> "123")))
    )

    val result = playlistCreateOrUpdate.toProto

    result mustEqual ProtoPlaylistCreateOrUpdate(
      description = Some("description"),
      ean = Some("ean"),
      genre = Some("genre"),
      labelName = Some("label name"),
      license = Some("license"),
      permalink = Some("permalink"),
      permalinkUrl = Some("permalink url"),
      public = Some(true),
      purchaseTitle = Some("purchase title"),
      release = Some("release"),
      releaseDate = Some("release date"),
      setType = Some("set type"),
      tagList = Some("tag list"),
      title = Some("title"),
      tracks = Some(ProtoTracks(urns = Seq("soundcloud:tracks:123")))
    )
  }

  "correctly missing values to proto" in new Scope {
    val playlistCreateOrUpdate = PlaylistCreateOrUpdate()

    val result = playlistCreateOrUpdate.toProto
    result mustEqual ProtoPlaylistCreateOrUpdate()
  }

  "correctly maps null values to proto" in new Scope {
    val playlistCreateOrUpdate = PlaylistCreateOrUpdate(
      description = NullValue,
      ean = NullValue,
      genre = NullValue,
      label_name = NullValue,
      license = NullValue,
      permalink = NullValue,
      permalink_url = NullValue,
      public = NullValue,
      purchase_title = NullValue,
      release = NullValue,
      release_date = NullValue,
      set_type = NullValue,
      tag_list = NullValue,
      title = NullValue,
      tracks = NullValue
    )

    val result = playlistCreateOrUpdate.toProto
    result mustEqual ProtoPlaylistCreateOrUpdate(
      description = Some(""),
      ean = Some(""),
      genre = Some(""),
      labelName = Some(""),
      license = Some(""),
      permalink = Some(""),
      permalinkUrl = Some(""),
      public = Some(false),
      purchaseTitle = Some(""),
      release = Some(""),
      releaseDate = Some(""),
      setType = Some(""),
      tagList = Some(""),
      title = Some(""),
      tracks = Some(ProtoTracks(urns = Seq.empty))
    )
  }
}
