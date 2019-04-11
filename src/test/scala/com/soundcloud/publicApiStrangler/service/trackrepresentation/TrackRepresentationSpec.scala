package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import org.joda.time.DateTime
import play.api.libs.json._

trait TrackRepresentationLikeSpecContext {
  implicit val trackRepresentationWrites = TrackRepresentation.writes

  val trackUrn = Urn("soundcloud", "tracks", "1324")
  val userUrn = Urn("soundcloud", "users", "3456")
  val labelUrn = Urn("soundcloud", "users", "999")

  def createTrackRepresentation(
                                 track: Track = defaultTrack,
                                 user: User = defaultUser,
                                 isrc: Option[Isrc] = defaultIsrc,
                                 counts: StitchCounts = defaultCounts,
                                 label: Option[User] = Some(defaultLabel),
                                 geoblockings: Geoblockings = defaultGeoblockings,
                                 domainlockings: Seq[DomainLocking] = defaultDomainLockings,
                                 audioMetadata: TrackAudioMetadata = defaultTrackAudioMetadata) =
    TrackRepresentation(track, user, isrc, counts, label, geoblockings, domainlockings, audioMetadata)

  def defaultLoggedInUserUrn = Urn("soundcloud", "users", "79241")

  def defaultTrackAudioMetadata =
    TrackAudioMetadata(
      state = "finished",
      original_format = Some("vqf"),
      original_content_size = Some(9001)
    )

  def defaultUser =
    User(
      urn = userUrn,
      permalink = "giraffe",
      username = "Dr. G. Raffe",
      avatar_url = "http://example.com/giraffe.jpg?123456789",
      permalink_url = "https://soundcloud.com/denis",
      city = None,
      country = None,
      tracks_count = 1,
      followers_count = Some(20000),
      followings_count = Some(20),
      verified = false,
      description = Some("I am a nice person"),
      updated_at = Some("2016/10/10 11:21:36 +0000"))

  def defaultLabelUrn = Some(labelUrn)

  def defaultLabel: User =
    User(
      urn = labelUrn,
      permalink = "raz",
      username = "Raz Putin",
      avatar_url = "http://example.com/raz.jpg?123456789",
      permalink_url = "https://soundcloud.com/raz",
      city = None,
      country = None,
      tracks_count = 4,
      followers_count = Some(10000),
      followings_count = Some(10),
      verified = true,
      description = Some("Psychonaut Music Inc."),
      updated_at = Some("2016/10/10 11:21:36 +0000"))

  def defaultTrack = Track(
    urn = trackUrn,
    user_urn = userUrn,
    commentable = false,
    description = Some("Follow @samstarling !"),
    created_at = new DateTime(2015, 2, 15, 16, 47, 27),
    disabled_at = None,
    downloadable = Some(false),
    duration = 120,
    genre = Some("future bass"),
    last_modified = new DateTime(2016, 8, 8, 13, 28, 53),
    permalink = "plsty-remix",
    permalink_url = Some("http://soundcloud.com/nirvana/plsty-remix"),
    public = true,
    secret_token = "s-53CR37",
    user_tags = List("dubstep", "folk", "tag with spaces"),
    machine_tags = List("system:foo", "system:bar", "awesomeness:very high"),
    title = "Baby Bash",
    uid = Some("a1b2c3"),
    api_streamable = Some(true),
    streamable = Some(false),
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
    label_id = defaultLabelUrn.map(_.identifier.toInt),
    supply_chain_status = Some("manual_upload")
  )

  def defaultIsrc = Some(Isrc("US-S1Z-99-00001"))

  def defaultCounts = StitchCounts(111, 222, 333, 444, 555)

  def defaultGeoblockings: Geoblockings = List("DE", "FR")

  def defaultDomainLockings: Seq[DomainLocking] = Seq(
    DomainLocking(
      domain = "example.com",
      trackUrn = trackUrn,
      urn = Urn("soundcloud", "domain-lockings", "97802143")))
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

    json \ "secret_token" ==== JsDefined(JsString("s-53CR37"))
    json \ "secret_uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324?secret_token=s-53CR37"))
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

    (json \ "available_country_codes").as[Vector[String]].sorted ==== Vector(
      "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AO", "AQ", "AR", "AS", "AT", "AU", "AW", "AX", "AZ",
      "BA", "BB", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BR", "BS",
      "BT", "BV", "BW", "BY", "BZ", "CA", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN",
      "CO", "CR", "CU", "CV", "CW", "CX", "CY", "CZ", "DJ", "DK", "DM", "DO", "DZ", "EC", "EE",
      "EG", "EH", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO", "GA", "GB", "GD", "GE", "GF",
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
      "VN", "VU", "WF", "WS", "YE", "YT", "ZA", "ZM", "ZW")
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
    labelJson \ "id" ==== JsDefined(JsNumber(999))
    labelJson \ "kind" ==== JsDefined(JsString("user"))
    labelJson \ "permalink" ==== JsDefined(JsString("raz"))
    labelJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/999"))
    labelJson \ "permalink_url" ==== JsDefined(JsString("https://soundcloud.com/raz"))
    labelJson \ "avatar_url" ==== JsDefined(JsString("https://example.com/raz.jpg"))
  }
}

class TrackRepresentationCountsDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationCountsDecorator.writes

    val counts = StitchCounts(111, 222, 333, 444, 555)
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationCountsDecorator(counts, wrapped)
  }

  "adds counts" in new Context {
    val json = Json.toJson(decorator)

    json \ "playback_count" ==== JsDefined(JsNumber(111))
    json \ "download_count" ==== JsDefined(JsNumber(222))
    json \ "favoritings_count" ==== JsDefined(JsNumber(333))
    json \ "reposts_count" ==== JsDefined(JsNumber(555))
  }
}

class TrackRepresentationCommentCountDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationCommentCountDecorator.writes

    val counts = StitchCounts(1, 2, 3, 444, 555)
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationCommentCountDecorator(counts, wrapped)
  }

  "adds counts" in new Context {
    val json = Json.toJson(decorator)

    json \ "comment_count" ==== JsDefined(JsNumber(444))
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

    json \ "user_favorite" ==== JsDefined(JsBoolean(true))
  }

  "adds user favourite information when track is not liked" in new Context {
    val isFavorite = false
    val decorator = TrackRepresentationUserFavoriteDecorator(isFavorite, wrapped)
    val json = Json.toJson(decorator)

    json \ "user_favorite" ==== JsDefined(JsBoolean(false))
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
    domainLockingJson(0) \ "domain" ==== JsDefined(JsString("example.com"))
  }
}

class TrackRepresentationUserPlaybackCountDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationUserPlaybackCountDecorator.writes

    val wrapped: TrackRepresentationLike = createTrackRepresentation()
    val decorator = TrackRepresentationUserPlaybackCountDecorator(wrapped)
  }

  "adds a user playback count of 1" in new Context {
    val json = Json.toJson(decorator)

    json \ "user_playback_count" ==== JsDefined(JsNumber(1))
  }
}

class TrackRepresentationWaveformUrlDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationWaveformUrlDecorator.writes
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
  }

  "adds the PNG URL of the track's 'stream' waveform" in new Context {
    val decorator = TrackRepresentationWaveformUrlDecorator(
      TrackWaveformUrl("some_uid", Url("https://bar.sndcdn.com/stream/a1b2c3.png")), wrapped)
    val json = Json.toJson(decorator)

    (json \ "waveform_url").as[String] ==== "https://bar.sndcdn.com/stream/a1b2c3.png"
  }
}

class TrackRepresentationAttachmentsUriDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationAttachmentsUriDecorator.writes
    val wrapped: TrackRepresentationLike = createTrackRepresentation()
  }

  "adds the attachments URI" in new Context {
    val decorator = TrackRepresentationAttachmentsUriDecorator(trackUrn, wrapped)
    val json = Json.toJson(decorator)

    (json \ "attachments_uri").as[String] ==== "https://api.soundcloud.com/tracks/1324/attachments"
  }
}

class TrackRepresentationSecretTokenUriParamDecoratorSpec extends UnitSpecification {
  "when all URL fields are present" >> {
    trait UrlsPresentContext extends Scope with TrackRepresentationLikeSpecContext {
      implicit val writes = TrackRepresentationSecretTokenUriParamDecorator.writes

      val wrapped = createTrackRepresentation()
      val decorator = TrackRepresentationSecretTokenUriParamDecorator(wrapped, "bl3rkbi3")
      val json = Json.toJson(decorator)
    }

    "adds the secret token to the URI" in new UrlsPresentContext {
      json \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324?secret_token=bl3rkbi3"))
    }

    "adds the secret token to the stream_url" in new UrlsPresentContext {
      json \ "stream_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324/stream?secret_token=bl3rkbi3"))
    }

    "adds the secret token to the download_url" in new UrlsPresentContext {
      json \ "download_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324/download?secret_token=bl3rkbi3"))
    }

    "adds the secret token to the permalink_url" in new UrlsPresentContext {
      json \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/nirvana/plsty-remix/bl3rkbi3"))
    }
  }

  // Some clients do this, according to our logs
  "when a particular URL field has a badly-encoded secret token" >> {
    trait BadlyFormedSecretTokenContext extends Scope with TrackRepresentationLikeSpecContext {
      implicit val writes = TrackRepresentationSecretTokenUriParamDecorator.writes

      val track = defaultTrack.copy()
      val wrapped = createTrackRepresentation(track = defaultTrack)
      val decorator = TrackRepresentationSecretTokenUriParamDecorator(wrapped, "badgers?format=json")
      val json = Json.toJson(decorator)
    }

    "correctly encodes it into the URI" in new BadlyFormedSecretTokenContext {
      json \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324?secret_token=badgers%3Fformat%3Djson"))
    }

    "correctly encodes it into the permalink_url" in new BadlyFormedSecretTokenContext {
      json \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/nirvana/plsty-remix/badgers%3Fformat%3Djson"))
    }
  }
}

class TrackRepresentationQuotaDecoratorSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext {
    implicit val writes = TrackRepresentationQuotaDecorator.writes

    val wrapped: TrackRepresentationLike = createTrackRepresentation()
  }

  "downloadable" >> {
    "when the track is downloadable" >> {
      "adds downloadable as true when downloads are below the user's quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 90, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(true))
      }

      "adds downloadable as false when downloads are exactly at the user's quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 100, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }

      "adds downloadable as false when downloads are above the user's quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 1000, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }

      "adds downloadable as true when the user has no quota (ie. unlimited)" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), None, 1000, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(true))
      }
    }

    "when the track is not downloadable" >> {
      "adds downloadable as false when downloads are below the user's quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(false), Some(100), 90, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }

      "adds downloadable as false when the user has no quota (ie. unlimited)" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(false), None, 90, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }
    }

    "when the track has a 'downloadable' value that is null (None)" >> {
      "adds downloadable as false when downloads are below the user's quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(None, Some(100), 90, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }

      "adds downloadable as false when the user has no quota (ie. unlimited)" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(None, None, 90, false, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloadable" ==== JsDefined(JsBoolean(false))
      }
    }
  }

  "downloads_remaining" >> {
    "when requesting user is the owner" >> {
      "adds number of downloads remaining, if the user has a quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 90, true, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloads_remaining" ==== JsDefined(JsNumber(10))
      }

      "adds number of downloads remaining, even if track has no downloads remaining" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 100, true, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloads_remaining" ==== JsDefined(JsNumber(0))
      }

      "adds number of downloads remaining, even if track is not downloadable" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(false), Some(100), 100, true, wrapped)
        val json = Json.toJson(decorator)
        json \ "downloads_remaining" ==== JsDefined(JsNumber(0))
      }

      "does not add number of downloads remaining, if the user has no quota (ie. unlimited)" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), None, 90, true, wrapped)
        val json = Json.toJson(decorator)
        json.as[JsObject].keys.contains("downloads_remaining") ==== false
      }
    }

    "when requesting user is not the owner" >> {
      "does not add number of downloads remaining, if the user has a quota" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), Some(100), 90, false, wrapped)
        val json = Json.toJson(decorator)
        json.as[JsObject].keys.contains("downloads_remaining") ==== false
      }

      "does not add number of downloads remaining, if the user has no quota (ie. unlimited)" in new Context {
        val decorator = TrackRepresentationQuotaDecorator(Some(true), None, 90, false, wrapped)
        val json = Json.toJson(decorator)
        json.as[JsObject].keys.contains("downloads_remaining") ==== false
      }
    }
  }
}

class TrackRepresentationSpec extends UnitSpecification {

  trait Context extends Scope with TrackRepresentationLikeSpecContext

  "serialises to JSON correctly" in new Context {
    val trackJson = Json.toJson(createTrackRepresentation())

    trackJson \ "kind" ==== JsDefined(JsString("track"))
    trackJson \ "id" ==== JsDefined(JsNumber(1324))
    trackJson \ "created_at" ==== JsDefined(JsString("2015/02/15 16:47:27 +0000"))
    trackJson \ "user_id" ==== JsDefined(JsNumber(3456))
    trackJson \ "duration" ==== JsDefined(JsNumber(120))
    trackJson \ "commentable" ==== JsDefined(JsBoolean(false))
    trackJson \ "last_modified" ==== JsDefined(JsString("2016/08/08 13:28:53 +0000"))
    trackJson \ "tag_list" ==== JsDefined(JsString("system:foo system:bar \"awesomeness:very high\" dubstep folk \"tag with spaces\""))
    trackJson \ "permalink" ==== JsDefined(JsString("plsty-remix"))
    trackJson \ "embeddable_by" ==== JsDefined(JsString("me"))
    trackJson \ "genre" ==== JsDefined(JsString("future bass"))
    trackJson \ "title" ==== JsDefined(JsString("Baby Bash"))
    trackJson \ "description" ==== JsDefined(JsString("Follow @samstarling !"))
    trackJson \ "label_name" ==== JsDefined(JsString("Denis Owns"))
    trackJson \ "isrc" ==== JsDefined(JsString("US-S1Z-99-00001"))
    trackJson \ "release_year" ==== JsDefined(JsNumber(1991))
    trackJson \ "release_month" ==== JsDefined(JsNumber(1))
    trackJson \ "release_day" ==== JsDefined(JsNumber(2))
    trackJson \ "license" ==== JsDefined(JsString("all-rights-reserved"))
    trackJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324"))
    trackJson \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/nirvana/plsty-remix"))
    trackJson \ "stream_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324/stream"))
    trackJson \ "download_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324/download"))
    trackJson \ "purchase_url" ==== JsDefined(JsString("http://example.com/buy/7890"))
    trackJson \ "purchase_title" ==== JsDefined(JsString("buy me pls"))
    trackJson \ "bpm" ==== JsDefined(JsNumber(120.7))
    trackJson \ "track_type" ==== JsDefined(JsString("original"))
    trackJson \ "release" ==== JsDefined(JsString("DR012"))
    trackJson \ "key_signature" ==== JsDefined(JsString("Emaj"))
    trackJson \ "video_url" ==== JsDefined(JsString("http://example.com/video.mp4"))
    trackJson \ "label_id" ==== JsDefined(JsNumber(999))
    trackJson \ "state" ==== JsDefined(JsString("finished"))
    trackJson \ "original_format" ==== JsDefined(JsString("vqf"))
    trackJson \ "original_content_size" ==== JsDefined(JsNumber(9001))

    val userJson = trackJson \ "user"
    userJson \ "id" ==== JsDefined(JsNumber(3456))
    userJson \ "kind" ==== JsDefined(JsString("user"))
    userJson \ "permalink" ==== JsDefined(JsString("giraffe"))
    userJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/3456"))
    userJson \ "permalink_url" ==== JsDefined(JsString("https://soundcloud.com/denis"))
    userJson \ "avatar_url" ==== JsDefined(JsString("https://example.com/giraffe.jpg"))
    userJson \ "last_modified" ==== JsDefined(JsString("2016/10/10 11:21:36 +0000"))
  }

  "streamable" >> {
    "true if api_streamable = true" in new Context {
      val trackJson = Json.toJson(createTrackRepresentation(track = defaultTrack.copy(api_streamable = Some(true))))
      trackJson \ "streamable" ==== JsDefined(JsBoolean(true))
    }

    "false if api_streamable = false" in new Context {
      val trackJson = Json.toJson(createTrackRepresentation(track = defaultTrack.copy(api_streamable = Some(false))))
      trackJson \ "streamable" ==== JsDefined(JsBoolean(false))
    }
  }

  "artwork_url" >> {
    "replaces original with large" in new Context {
      val artwork = Artwork(filename = Some("donkey-original.jpg"))
      val trackJson = Json.toJson(createTrackRepresentation(track = defaultTrack.copy(artwork = artwork)))
      trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/donkey-large.jpg"))
    }

    "replaces png with jpg" in new Context {
      val artwork = Artwork(filename = Some("donkey-original.png"))
      val trackJson = Json.toJson(createTrackRepresentation(track = defaultTrack.copy(artwork = artwork)))
      trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/donkey-large.jpg"))
    }
  }

  "sharing" in new Context {
    val publicTrack = defaultTrack.copy(public = true)
    val publicTrackRepresentation = createTrackRepresentation(track = publicTrack)
    val publicTrackJson = Json.toJson(publicTrackRepresentation)
    publicTrackJson \ "sharing" ==== JsDefined(JsString("public"))

    val privateTrack = defaultTrack.copy(public = false)
    val privateTrackRepresentation = createTrackRepresentation(track = privateTrack)
    val privateTrackJson = Json.toJson(privateTrackRepresentation)
    privateTrackJson \ "sharing" ==== JsDefined(JsString("private"))
  }

  "strangely specific bpm values" in new Context {
    val track = defaultTrack.copy(bpm = Some(128.10000610351562))
    val trackRep = createTrackRepresentation(track = track)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "bpm" ==== JsDefined(JsNumber(128.1))
  }

  "release year, but no release day/month" in new Context {
    val track = defaultTrack.copy(release_year = Some(2016), release_month = None, release_day = None)
    val trackRep = createTrackRepresentation(track = track)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "release_year" ==== JsDefined(JsNumber(2016))
    trackJson \ "release_month" ==== JsDefined(JsNumber(1))
    trackJson \ "release_day" ==== JsDefined(JsNumber(1))
  }

  "no release year" in new Context {
    val track = defaultTrack.copy(release_year = None, release_month = Some(2))
    val trackRep = createTrackRepresentation(track = track)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "release_year" ==== JsDefined(JsNull)
    trackJson \ "release_month" ==== JsDefined(JsNull)
    trackJson \ "release_day" ==== JsDefined(JsNull)
  }

  "weird artwork filename" in new Context {
    val track = defaultTrack.copy(artwork = Artwork(filename = Some("adfhlsh.jpg")))
    val trackRep = createTrackRepresentation(track = track)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/adfhlsh.jpg"))
  }

  "user avatar URL with HTTP and no trailing number" in new Context {
    val user = defaultUser.copy(avatar_url = "http://example.com/img.png")
    val trackRep = createTrackRepresentation(track = defaultTrack, user = user)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
  }

  "user avatar URL with HTTP and trailing number" in new Context {
    val user = defaultUser.copy(avatar_url = "http://example.com/img.png?123")
    val trackRep = createTrackRepresentation(track = defaultTrack, user = user)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
  }

  "user avatar URL with HTTPS and trailing number" in new Context {
    val user = defaultUser.copy(avatar_url = "https://example.com/img.png?123")
    val trackRep = createTrackRepresentation(track = defaultTrack, user = user)
    val trackJson = Json.toJson(trackRep)
    trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
  }
}
