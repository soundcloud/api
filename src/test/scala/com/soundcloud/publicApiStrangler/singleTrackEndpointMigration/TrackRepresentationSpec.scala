package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import org.joda.time.LocalDateTime

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import play.api.libs.json.Json

class TrackRepresentationSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackUrn = new Urn("soundcloud", "tracks", "1324")
    val userUrn = new Urn("soundcloud", "users", "3456")
  }

  "serialises to JSON correctly" in new Context {
    val track = Track(
      urn = trackUrn,
      user_urn = Urn("soundcloud:users:112"),
      commentable = false,
      description = None,
      created_at = new LocalDateTime(2015, 2, 15, 16, 47, 27),
      disabled_at = None,
      downloadable = false,
      duration = 120,
      genre = None,
      last_modified = new LocalDateTime(2016, 8, 8, 13, 28, 53),
      permalink = null,
      permalink_url = None,
      public = true,
      secret_token = null,
      user_tags = List("dubstep", "folk"),
      machine_tags = List("system:foo", "system:bar"),
      title = null,
      uid = None,
      api_streamable = None,
      streamable = false,
      reveal_comments = false,
      reveal_stats = false,
      label_name = None,
      license = null,
      embeddable = None,
      release_year = None,
      release_month = None,
      release_day = None,
      embeddableBy = EmbeddingPermission.Me,
      releaseDate = None,
      artwork = Artwork(filename = Some("http://example.com/art/work.jpg")),
      published_at = None)
    val trackRepresentation = TrackRepresentation(track, userUrn)

    Json.toJson(trackRepresentation) ==== Json.obj(
      "kind" -> "track",
      "id" -> 1324,
      "created_at" -> "2015/02/15 16:47:27 +0000",
      "user_id" -> 3456,
      "duration" -> 120,
      "commentable" -> false,
      "last_modified" -> "2016/08/08 13:28:53 +0000",
      "embeddable_by" -> "me",
      "tag_list" -> "dubstep, folk, system:foo, system:bar",
      "artwork_url" -> "http://example.com/art/work.jpg"
    )
  }
}
