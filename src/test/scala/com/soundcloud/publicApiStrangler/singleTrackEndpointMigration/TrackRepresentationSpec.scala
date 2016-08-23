package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.User
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsBoolean, JsNumber, JsString, Json}

class TrackRepresentationSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackUrn = new Urn("soundcloud", "tracks", "1324")
    val userUrn = new Urn("soundcloud", "users", "3456")

    val defaultUser =
      User(
        urn = userUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "https://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"))

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
      permalink_url = Some("http://soundcloud.com/nirvana/plsty-remix"),
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
      license = "all-rights-reserved",
      embeddable = None,
      release_year = Some(1991),
      release_month = Some(1),
      release_day = Some(2),
      embeddableBy = EmbeddingPermission.Me,
      releaseDate = None,
      artwork = Artwork(filename = Some("artworks-FuwbhSJORvKH-0-original.jpg")),
      published_at = None)
  }

  "serialises to JSON correctly" in new Context {
    val trackRepresentation = TrackRepresentation(defaultTrack, defaultUser)
    val trackJson = Json.toJson(trackRepresentation)

    trackJson \ "kind" ==== JsString("track")
    trackJson \ "id" ==== JsNumber(1324)
    trackJson \ "created_at" ==== JsString("2015/02/15 16:47:27 +0000")
    trackJson \ "user_id" ==== JsNumber(3456)
    trackJson \ "duration" ==== JsNumber(120)
    trackJson \ "commentable" ==== JsBoolean(false)
    trackJson \ "last_modified" ==== JsString("2016/08/08 13:28:53 +0000")
    trackJson \ "tag_list" ==== JsString("dubstep, folk, system:foo, system:bar")
    trackJson \ "permalink" ==== JsString("plsty-remix")
    trackJson \ "streamable" ==== JsBoolean(false)
    trackJson \ "embeddable_by" ==== JsString("me")
    trackJson \ "downloadable" ==== JsBoolean(false)
    trackJson \ "genre" ==== JsString("future bass")
    trackJson \ "title" ==== JsString("Baby Bash")
    trackJson \ "description" ==== JsString("Follow @samstarling !")
    trackJson \ "label_name" ==== JsString("Denis Owns")
    trackJson \ "release_year" ==== JsNumber(1991)
    trackJson \ "release_month" ==== JsNumber(1)
    trackJson \ "release_day" ==== JsNumber(2)
    trackJson \ "license" ==== JsString("all-rights-reserved")
    trackJson \ "uri" ==== JsString("https://api.soundcloud.com/tracks/1324")
    trackJson \ "user" \ "id" ==== JsNumber(3456)
    trackJson \ "user" \ "kind" ==== JsString("user")
    trackJson \ "user" \ "permalink" ==== JsString("giraffe")
    trackJson \ "permalink_url" ==== JsString("http://soundcloud.com/nirvana/plsty-remix")
    trackJson \ "artwork_url" ==== JsString("https://i1.sndcdn.com/artworks-FuwbhSJORvKH-0-original.jpg")

    val userJson = trackJson \ "user"

    userJson \ "id" ==== JsNumber(3456)
    userJson \ "kind" ==== JsString("user")
    userJson \ "permalink" ==== JsString("giraffe")
    userJson \ "uri" ==== JsString("https://api.soundcloud.com/users/3456")
    userJson \ "permalink_url" ==== JsString("https://soundcloud.com/denis")
    userJson \ "avatar_url" ==== JsString("http://example.com/giraffe.jpg")
  }

  "sharing" in new Context {
    val publicTrack = defaultTrack.copy(public = true)
    val publicTrackRepresentation = TrackRepresentation(publicTrack, defaultUser)
    val publicTrackJson = Json.toJson(publicTrackRepresentation)
    publicTrackJson \ "sharing" ==== JsString("public")

    val privateTrack = defaultTrack.copy(public = false)
    val privateTrackRepresentation = TrackRepresentation(privateTrack, defaultUser)
    val privateTrackJson = Json.toJson(privateTrackRepresentation)
    privateTrackJson \ "sharing" ==== JsString("private")
  }
}
