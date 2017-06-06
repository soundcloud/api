package com.soundcloud.publicApiStrangler.client.mothership.response.mapper

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._

class TrackMapperSpec extends UnitSpecification {
  "Maps a track" >> {

    "All fields" in {
      val presented = TrackMapper(moshiTrackFull)

      (presented.urn) must be_==(Urn("soundcloud:sounds:174088262"))
      (presented.user_urn) must be_==(Urn("soundcloud:users:102661606"))

      (presented.api_streamable) must be_==(Some(true))
      (presented.artwork_url) must be_==(Some("https://i1.sndcdn.com/artworks-000095281756-51d163-large.jpg"))
      (presented.bucket) must be_==(Some("soundcloud-media"))
      (presented.commentable) must be_==(true)
      (presented.comments_count) must be_==(Some(0))
      (presented.created_at) must be_==("2014/10/27 16:25:25 +0000")
      (presented.description) must be_==(Some("This track is awesome"))
      (presented.disabled_at) must be_==(Some("2014/08/27 00:11:22 +0000"))
      (presented.disabled_reason) must be_==(Some("blacklisted"))
      (presented.download_url) must be_==(Some("https://api.soundcloud.com/tracks/174088262/download"))
      (presented.downloadable) must be_==(Some(false))
      (presented.downloads_count) must be_==(Some(0))
      (presented.duration) must be_==(0)
      (presented.embeddable) must be_==(Some(true))
      (presented.embeddable_by) must be_==(Some("all"))
      (presented.favoritings_count) must be_==(Some(0))
      (presented.feedable) must be_==(Some(false))
      (presented.genre) must be_==(Some("Free jazz"))
      (presented.geo_blocking) must be_==(Some(false))
      (presented.isrc) must be_==(Some("US-S1Z-99-00001"))
      (presented.label_id) must be_==(None)
      (presented.label_name) must be_==(Some("Foobar records"))
      (presented.last_modified) must be_==("2014/10/27 16:25:25 +0000")
      (presented.license) must be_==(Some("all-rights-reserved"))
      (presented.original_artwork_url) must be_==(Some("https://i1.sndcdn.com/artworks-000095281756-51d163-original.png"))
      (presented.original_content_size) must be_==(Some(6923))
      (presented.original_format) must be_==(Some("mp3"))
      (presented.permalink) must be_==("awesome-track-2014-10-27-17-25-29-66")
      (presented.permalink_url) must be_==("https://soundcloud.com/imprisonedprecision/awesome-track-2014-10-27-17-25-29-66")
      (presented.playback_count) must be_==(Some(0))
      (presented.public) must be_==(true)
      (presented.release_date) must be_==(Some("2013-02-01"))
      (presented.release_day) must be_==(Some(1))
      (presented.release_month) must be_==(Some(2))
      (presented.release_year) must be_==(Some(2013))
      (presented.reposts_count) must be_==(Some(0))
      (presented.reveal_comments) must be_==(Some(true))
      (presented.reveal_stats) must be_==(Some(true))
      (presented.secret_token) must be_==(Some("s-8USae"))
      (presented.sharing) must be_==("public")
      (presented.state) must be_==("storing")
      (presented.stream_url) must be_==(Some("https://media.soundcloud.com/stream/2014-10-27-17-25-29-66"))
      (presented.streamable) must be_==(true)
      (presented.tag_list) must be_==(Some("tag onw two"))
      (presented.title) must be_==("Awesome Track")
      (presented.track_type) must be_==(None)
      (presented.uid) must be_==(Some("2014-10-27-17-25-29-66"))
      presented.updated_at ==== "2014/10/27 16:25:25 +0000"
      presented.uri ==== "https://api.soundcloud.com/tracks/174088262"
      (presented.waveform_url) must be_==("https://wis.sndcdn.com/images/player-waveform-medium.png?1414404638")
      (presented.has_downloads_left) must be_==(true)
    }
  }

  "Full with no downloads left" in {
    val presented = TrackMapper(moshiTrackFullWithNoDownloadsLeft)
    (presented.has_downloads_left) must be_==(false)
  }

  "Full without downloads left defaults to false" in {
    val presented = TrackMapper(moshiTrackFullWithoutDownloadsLeft)
    (presented.has_downloads_left) must be_==(false)
  }

  "Minimal fields" in {
    val presented = TrackMapper(moshiTrackMinimal)
    (presented.urn) must be_==(Urn("soundcloud:sounds:174090825"))
  }
}
