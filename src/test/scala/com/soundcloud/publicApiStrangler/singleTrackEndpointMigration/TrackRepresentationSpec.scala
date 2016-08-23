package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsBoolean, JsNumber, JsString, Json}

class TrackRepresentationSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackUrn = new Urn("soundcloud", "tracks", "1324")
    val userUrn = new Urn("soundcloud", "users", "3456")
    val defaultTrack = Track(
      urn = trackUrn,
      user_urn = Urn("soundcloud:users:112"),
      commentable = false,
      description = Some("Follow @samstarling !"),
      created_at = new LocalDateTime(2015, 2, 15, 16, 47, 27),
      disabled_at = None,
      downloadable = false,
      duration = 120,
      genre = Some("future bass"),
      last_modified = new LocalDateTime(2016, 8, 8, 13, 28, 53),
      permalink = "plsty-remix",
      permalink_url = None,
      public = true,
      secret_token = null,
      user_tags = List("dubstep", "folk"),
      machine_tags = List("system:foo", "system:bar"),
      title = "Baby Bash",
      uid = None,
      api_streamable = None,
      streamable = false,
      reveal_comments = false,
      reveal_stats = false,
      label_name = Some("Denis Owns"),
      license = null,
      embeddable = None,
      release_year = None,
      release_month = None,
      release_day = None,
      embeddableBy = EmbeddingPermission.Me,
      releaseDate = None,
      artwork = Artwork(filename = Some("http://example.com/art/work.jpg")),
      published_at = None)
  }

  "serialises to JSON correctly" in new Context {
    val trackRepresentation = TrackRepresentation(defaultTrack, userUrn)
    val trackJson = Json.toJson(trackRepresentation)

    trackJson \ "kind" ==== JsString("track")
    trackJson \ "id" ==== JsNumber(1324)
    trackJson \ "created_at" ==== JsString("2015/02/15 16:47:27 +0000")
    trackJson \ "user_id" ==== JsNumber(3456)
    trackJson \ "duration" ==== JsNumber(120)
    trackJson \ "commentable" ==== JsBoolean(false)
    trackJson \ "last_modified" ==== JsString("2016/08/08 13:28:53 +0000")
    trackJson \ "permalink" ==== JsString("plsty-remix")
    trackJson \ "streamable" ==== JsBoolean(false)
    trackJson \ "embeddable_by" ==== JsString("me")
    trackJson \ "downloadable" ==== JsBoolean(false)
    trackJson \ "genre" ==== JsString("future bass")
    trackJson \ "title" ==== JsString("Baby Bash")
    trackJson \ "description" ==== JsString("Follow @samstarling !")
    trackJson \ "label_name" ==== JsString("Denis Owns")
    trackJson \ "tag_list" ==== JsString("dubstep, folk, system:foo, system:bar")
    trackJson \ "artwork_url" ==== JsString("http://example.com/art/work.jpg")
  }

  "sharing" in new Context {
    val publicTrack = defaultTrack.copy(public = true)
    val publicTrackRepresentation = TrackRepresentation(publicTrack, userUrn)
    val publicTrackJson = Json.toJson(publicTrackRepresentation)
    publicTrackJson \ "sharing" ==== JsString("public")

    val privateTrack = defaultTrack.copy(public = false)
    val privateTrackRepresentation = TrackRepresentation(privateTrack, userUrn)
    val privateTrackJson = Json.toJson(privateTrackRepresentation)
    privateTrackJson \ "sharing" ==== JsString("private")
  }
}
