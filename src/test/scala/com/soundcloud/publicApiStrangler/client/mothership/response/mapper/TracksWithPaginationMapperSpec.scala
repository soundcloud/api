package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.TrackCursor
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import play.api.libs.json.JsObject

class TracksWithPaginationMapperSpec extends UnitSpecification {
  "Maps a track" >> {

    "All fields" in {
      val presented = TracksWithPaginationMapper(moshiPlaylistTracksWithPagination)

      val expectedTrack = TrackMapper((moshiPlaylistTracksWithPagination \ "tracks").as[List[JsObject]].head)
      presented.tracks must haveSize(1)
      presented.tracks.head.urn ==== expectedTrack.urn
      presented.tracks.head.duration ==== expectedTrack.duration

      presented.meta.cursor must beSome[TrackCursor]
      val cursor = presented.meta.cursor.get

      cursor.nextHref ==== "http://moshimoshi.int.s-cloud.net/playlists/soundcloud:playlists:1231/tracks_with_pagination?after=33678\u0026limit=1"
      cursor.after ==== 33678
      cursor.limit ==== 1
    }
  }

  "Minimal fields" in {
    val presented = TrackMapper(moshiTrackMinimal)

    (presented.urn) must be_==(Urn("soundcloud", "tracks", "174090825"))
  }
}
