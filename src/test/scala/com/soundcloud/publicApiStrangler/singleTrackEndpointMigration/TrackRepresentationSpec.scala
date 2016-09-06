package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.bff.JsObject
import com.soundcloud.jvmkit.UserSession
import com.soundcloud.publicApiStrangler.client.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.service.response.representation.{Geoblockings, User}
import org.joda.time.LocalDateTime
import play.api.libs.json._

trait TrackRepresentationLikeSpecContext {
  implicit val trackRepresentationWrites = TrackRepresentation.writes

  val trackUrn = new Urn("soundcloud", "tracks", "1324")
  val userUrn = new Urn("soundcloud", "users", "3456")
  val labelUrn = new Urn("soundcloud", "users", "999")

  def createTrackRepresentation(
    track: Track = defaultTrack,
    user: User = defaultUser,
    isrc: Option[Isrc] = defaultIsrc,
    counts: StitchCounts = defaultCounts,
    label: Option[User] = Some(defaultLabel),
    geoblockings: Option[Geoblockings] = Some(defaultGeoblockings),
    domainlockings: Seq[DomainLocking] = defaultDomainLockings,
    audioMetadata: TrackAudioMetadata = defaultTrackAudioMetadata) =
    TrackRepresentation(
      track, user, isrc, counts, label, geoblockings, domainlockings, audioMetadata)

  def defaultLoggedInUserUrn = new Urn("soundcloud", "users", "79241")

  def defaultTrackAudioMetadata =
    TrackAudioMetadata(
      state = "finished",
      original_format = "vqf",
      original_content_size = 9001
    )

  def defaultUser =
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

  def defaultLabelUrn = Some(labelUrn)

  def defaultLabel: User =
    User(
      urn = labelUrn,
      permalink = "raz",
      username = "Raz Putin",
      avatar_url = "http://example.com/raz.jpg",
      permalink_url = "https://soundcloud.com/raz",
      city = None,
      country = None,
      tracks_count = 4,
      followers_count = Some(10000),
      followings_count = Some(10),
      verified = true,
      description = Some("Psychonaut Music Inc."))

  def defaultTrack = Track(
    urn = trackUrn,
    user_urn = userUrn,
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
    secret_token = "s-53CR37",
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
    published_at = None,
    purchase_url = Some("http://example.com/buy/7890"),
    purchase_title = Some("buy me pls"),
    bpm = Some(120.7),
    track_type = Some("original"),
    release = Some("DR012"),
    key_signature = Some("Emaj"),
    video_url = Some("http://example.com/video.mp4"),
    label_id = defaultLabelUrn.map(_.getIdentifier.toInt)
  )

  def defaultIsrc = Some(Isrc("US-S1Z-99-00001"))

  def defaultCounts = StitchCounts(111, 222, 333, 444)

  def defaultGeoblockings: Geoblockings = List("DE", "FR")

  def defaultDomainLockings: Seq[DomainLocking] = Seq(
    DomainLocking(
      domain = "example.com",
      trackUrn = trackUrn,
      urn = Urn("soundcloud:domain-lockings:97802143")))
}

class TrackRepresentationSecretTokenDecoratorSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationSecretTokenDecorator.writes

    val track: Track = defaultTrack
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationSecretTokenDecorator(track, wrapped)
  }

  "adds secret token stuff" in new Context {
    val json = Json.toJson(decorator)

    json \ "secret_token" ==== JsString("s-53CR37")
    json \ "secret_url" ==== JsString("https://api.soundcloud.com/tracks/1324?secret_token=s-53CR37")
  }
}

class TrackRepresentationGeoblockingsDecoratorSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationGeoblockingsDecorator.writes

    val geoblockings: Geoblockings = List("DE", "FR")
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationGeoblockingsDecorator(geoblockings, wrapped)
  }

  "adds geoblocking info" in new Context {
    val json = Json.toJson(decorator)

    (json \ "available_country_codes").as[JsArray].value.sortBy(_.as[JsString].value) ==== Seq(
      "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ",
      "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS",
      "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN",
      "CO", "CR", "CU", "CV", "CW", "CX", "CY", "CZ",       "DJ", "DK", "DM", "DO", "DZ", "EC", "EE",
      "EG", "EH", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO",       "GA", "GB", "GD", "GE", "GF",
      "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS", "GT", "GU", "GW", "GY", "HK", "HM",
      "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IM", "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM",
      "JO", "JP", "KE", "KG", "KH", "KI", "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC",
      "LI", "LK", "LR", "LS", "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK",
      "ML", "MM", "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA",
      "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE", "PF", "PG",
      "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE", "RO", "RS", "RU", "RW",
      "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK", "SL", "SM", "SN", "SO", "SR", "SS",
      "ST", "SV", "SX", "SY", "SZ", "TC", "TD", "TF", "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO",
      "TR", "TT", "TV", "TW", "TZ", "UA", "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI",
      "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW").map(JsString(_))
  }
}

class TrackRepresentationLabelDecoratorSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationLabelDecorator.writes

    val label: User = defaultLabel
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationLabelDecorator(label, wrapped)
  }

  "adds label info" in new Context {
    val json = Json.toJson(decorator)

    val labelJson = json \ "label"
    labelJson \ "id" ==== JsNumber(999)
    labelJson \ "kind" ==== JsString("user")
    labelJson \ "permalink" ==== JsString("raz")
    labelJson \ "uri" ==== JsString("https://api.soundcloud.com/users/999")
    labelJson \ "permalink_url" ==== JsString("https://soundcloud.com/raz")
    labelJson \ "avatar_url" ==== JsString("http://example.com/raz.jpg")
  }
}

class TrackRepresentationUserFavoriteDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationUserFavoriteDecorator.writes

    val wrapped: TrackRepresentationLike = createTrackRepresentation()
  }

  "adds user favourite information when track is liked" in new Context {
    val isFavorite = true
    val decorator = TrackRepresentationUserFavoriteDecorator(isFavorite, wrapped)
    val json = Json.toJson(decorator)

    json \ "user_favorite" ==== JsString("true")
  }

  "adds user favourite information when track is not liked" in new Context {
    val isFavorite = false
    val decorator = TrackRepresentationUserFavoriteDecorator(isFavorite, wrapped)
    val json = Json.toJson(decorator)

    json \ "user_favorite" ==== JsString("false")
  }
}

class TrackRepresentationDomainLockingsDecoratorSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationDomainLockingsDecorator.writes

    val domainLockings = defaultDomainLockings
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationDomainLockingsDecorator(domainLockings, wrapped)
  }

  "adds domain locking info" in new Context {
    val json = Json.toJson(decorator)

    val domainLockingJson = json \ "domain_lockings"
    domainLockingJson(0) \ "domain" ==== JsString("example.com")
  }
}

class TrackRepresentationSpec extends UnitSpecification {
  trait Context extends Scope with TrackRepresentationLikeSpecContext

  "serialises to JSON correctly" in new Context {
    val trackJson = Json.toJson(createTrackRepresentation())

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
    trackJson \ "isrc" ==== JsString("US-S1Z-99-00001")
    trackJson \ "release_year" ==== JsNumber(1991)
    trackJson \ "release_month" ==== JsNumber(1)
    trackJson \ "release_day" ==== JsNumber(2)
    trackJson \ "license" ==== JsString("all-rights-reserved")
    trackJson \ "uri" ==== JsString("https://api.soundcloud.com/tracks/1324")
    trackJson \ "permalink_url" ==== JsString("http://soundcloud.com/nirvana/plsty-remix")
    trackJson \ "artwork_url" ==== JsString("https://i1.sndcdn.com/artworks-FuwbhSJORvKH-0-original.jpg")
    trackJson \ "stream_url" ==== JsString("https://api.soundcloud.com/tracks/1324/stream")
    trackJson \ "download_url" ==== JsString("https://api.soundcloud.com/tracks/1324/download")
    trackJson \ "purchase_url" ==== JsString("http://example.com/buy/7890")
    trackJson \ "purchase_title" ==== JsString("buy me pls")
    trackJson \ "bpm" ==== JsNumber(120.7)
    trackJson \ "track_type" ==== JsString("original")
    trackJson \ "release" ==== JsString("DR012")
    trackJson \ "key_signature" ==== JsString("Emaj")
    trackJson \ "video_url" ==== JsString("http://example.com/video.mp4")
    trackJson \ "label_id" ==== JsNumber(999)
    trackJson \ "playback_count" ==== JsNumber(111)
    trackJson \ "download_count" ==== JsNumber(222)
    trackJson \ "favoritings_count" ==== JsNumber(333)
    trackJson \ "comment_count" ==== JsNumber(444)
    trackJson \ "state" ==== JsString("finished")
    trackJson \ "original_format" ==== JsString("vqf")
    trackJson \ "original_content_size" ==== JsNumber(9001)

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
    val publicTrackRepresentation = createTrackRepresentation(track = publicTrack)
    val publicTrackJson = Json.toJson(publicTrackRepresentation)
    publicTrackJson \ "sharing" ==== JsString("public")

    val privateTrack = defaultTrack.copy(public = false)
    val privateTrackRepresentation = createTrackRepresentation(track = privateTrack)
    val privateTrackJson = Json.toJson(privateTrackRepresentation)
    privateTrackJson \ "sharing" ==== JsString("private")
  }
}
