package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import play.api.libs.json.JsObject

class PlaylistMapperSpec extends UnitSpecification {
  "maps attributes to object" in {
    val playlist = PlaylistMapper(moshiPlaylist.as[JsObject])

    playlist.urn ==== Urn("soundcloud", "playlists", "42703821")
    playlist.user_urn ==== Urn("soundcloud", "users", "10419549")
    playlist.title ==== "dub dub dub dub"
    playlist.created_at ==== "2014/07/07 13:32:46 +0000"
    playlist.duration ==== 308351
    playlist.genre ==== ""
    playlist.permalink_url ==== "https://soundcloud.com/h_freeman/sets/dub-dub-dub"
    playlist.artwork_url ==== ""
    playlist.track_count ==== 1
    playlist.likes_count ==== 0
    playlist.reposts_count ==== 0
    playlist.sharing ==== "public"
    playlist.tag_list ==== ""
  }

  "handles durations bigger than MAX_INT" in {
    val playlist = PlaylistMapper(moshiPlaylistLongDuration.as[JsObject])
    playlist.duration ==== 2206478654L
  }
}
