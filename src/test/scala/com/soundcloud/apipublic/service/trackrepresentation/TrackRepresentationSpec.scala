package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.apipublic.authorization.AllowlistedClients
import com.soundcloud.apipublic.authorization.policies._
import com.soundcloud.apipublic.client.mothership.response.representation.{Geoblockings, UserRepresentation}
import com.soundcloud.apipublic.client.trackcoordinator.TrackCoordinatorTrack
import com.soundcloud.apipublic.client.tracks._
import com.soundcloud.apipublic.service.users.UserBuilder
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures
import org.joda.time.LocalDateTime
import play.api.libs.json.{JsDefined, _}

trait TrackRepresentationSpecContext {
  val trackUrn = Urn("soundcloud", "tracks", "1324")
  val userUrn = Urn("soundcloud", "users", "3456")
  val labelUrn = Urn("soundcloud", "users", "999")

  def createTrackRepresentationFromVisibleTrack(
      client: Urn = Urn("soundcloud", "applications", "123"),
      loggedInUser: Urn = Urn("soundcloud", "users", "555"),
      visibleTrack: VisibleTrack = defaultTrack,
      user: UserRepresentation = defaultUser,
      geoblockings: Geoblockings = defaultGeoblockings,
      isLiked: Boolean = false
  ) = {
    TrackRepresentationBuilder.fromVisibleTrack(
      agent = Some(client),
      sessionUser = Some(loggedInUser),
      visibleTrack = visibleTrack,
      user = user,
      geoblockings = geoblockings,
      isLiked = isLiked
    )
  }

  def defaultLoggedInUserUrn = Urn("soundcloud", "users", "79241")

  def defaultUser = new UserBuilder().setUrn(userUrn).build

  def defaultLabel: UserRepresentation = new UserBuilder().setUrn(labelUrn).build

  def defaultTrack = VisibleTrack(
    urn = trackUrn,
    userUrn = userUrn,
    commentable = false,
    description = Some("Follow @samstarling !"),
    createdAt = new LocalDateTime(2015, 2, 15, 16, 47, 27),
    disabledAt = None,
    downloadable = true,
    duration = 60000,
    genre = Some("future bass"),
    permalinkUrl = Some("http://soundcloud.com/nirvana/plsty-remix"),
    public = true,
    secretToken = Some("s-53CR37"),
    userTags = List("dubstep", "folk", "tag with spaces"),
    machineTags = List("system:foo", "system:bar", "awesomeness:very high"),
    title = "Baby Bash",
    uid = Some("a1b2c3"),
    apiStreamable = Some(true),
    streamable = false,
    revealComments = false,
    revealStats = false,
    labelName = Some("Denis Owns"),
    license = "all-rights-reserved",
    embeddable = None,
    releaseYear = Some(1991),
    releaseMonth = Some(1),
    releaseDay = Some(2),
    embeddableBy = EmbeddingPermission.Me,
    releaseDate = None,
    artwork = Artwork(filename = Some("artworks-FuwbhSJORvKH-0-original.jpg")),
    publishedAt = None,
    purchaseUrl = Some("http://example.com/buy/7890"),
    purchaseTitle = Some("buy me pls"),
    bpm = Some(120.7),
    release = Some("DR012"),
    keySignature = Some("Emaj"),
    supplyChainStatus = Some("manual_upload"),
    authorization = new ContentAuthorization(
      trackUrn,
      ContentPolicy.MONETIZE,
      Reason.NOT_SUPPORTED,
      ContentRestriction.ENCRYPTED_STREAM_ONLY,
      MonetizationModel.AD_SUPPORTED
    ),
    transcodings = List.empty[Transcoding],
    waveformUrls = List(
      WaveformUrl(
        WaveformType.Full,
        Url("https://bar.sndcdn.com/stream/a1b2c3.json"),
        Url("https://bar.sndcdn.com/stream/a1b2c3.png")
      )
    ),
    access = Some(Access.Playable),
    counts = VisibleTrackCounts(None, None, None, None, None),
    isrc = defaultIsrc,
    metaDataArtist = Option.empty
  )

  def defaultIsrc = Some("US-S1Z-99-00001")

  def defaultGeoblockings: Geoblockings = List("DE", "FR")
}

class TrackRepresentationSpec extends UnitSpecification {
  "geoblocking" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val geoblockings: Geoblockings = List("DE", "FR")
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(geoblockings = geoblockings)
    }

    "adds geoblocking info" in new Context {
      val json = Json.toJson(trackRepresentation)

      (json \ "available_country_codes").as[Vector[String]].sorted ==== Vector(
        "AD",
        "AE",
        "AF",
        "AG",
        "AI",
        "AL",
        "AM",
        "AO",
        "AQ",
        "AR",
        "AS",
        "AT",
        "AU",
        "AW",
        "AX",
        "AZ",
        "BA",
        "BB",
        "BD",
        "BE",
        "BF",
        "BG",
        "BH",
        "BI",
        "BJ",
        "BL",
        "BM",
        "BN",
        "BO",
        "BQ",
        "BR",
        "BS",
        "BT",
        "BV",
        "BW",
        "BY",
        "BZ",
        "CA",
        "CC",
        "CD",
        "CF",
        "CG",
        "CH",
        "CI",
        "CK",
        "CL",
        "CM",
        "CN",
        "CO",
        "CR",
        "CU",
        "CV",
        "CW",
        "CX",
        "CY",
        "CZ",
        "DJ",
        "DK",
        "DM",
        "DO",
        "DZ",
        "EC",
        "EE",
        "EG",
        "EH",
        "ER",
        "ES",
        "ET",
        "FI",
        "FJ",
        "FK",
        "FM",
        "FO",
        "GA",
        "GB",
        "GD",
        "GE",
        "GF",
        "GG",
        "GH",
        "GI",
        "GL",
        "GM",
        "GN",
        "GP",
        "GQ",
        "GR",
        "GS",
        "GT",
        "GU",
        "GW",
        "GY",
        "HK",
        "HM",
        "HN",
        "HR",
        "HT",
        "HU",
        "ID",
        "IE",
        "IL",
        "IM",
        "IN",
        "IO",
        "IQ",
        "IR",
        "IS",
        "IT",
        "JE",
        "JM",
        "JO",
        "JP",
        "KE",
        "KG",
        "KH",
        "KI",
        "KM",
        "KN",
        "KP",
        "KR",
        "KW",
        "KY",
        "KZ",
        "LA",
        "LB",
        "LC",
        "LI",
        "LK",
        "LR",
        "LS",
        "LT",
        "LU",
        "LV",
        "LY",
        "MA",
        "MC",
        "MD",
        "ME",
        "MF",
        "MG",
        "MH",
        "MK",
        "ML",
        "MM",
        "MN",
        "MO",
        "MP",
        "MQ",
        "MR",
        "MS",
        "MT",
        "MU",
        "MV",
        "MW",
        "MX",
        "MY",
        "MZ",
        "NA",
        "NC",
        "NE",
        "NF",
        "NG",
        "NI",
        "NL",
        "NO",
        "NP",
        "NR",
        "NU",
        "NZ",
        "OM",
        "PA",
        "PE",
        "PF",
        "PG",
        "PH",
        "PK",
        "PL",
        "PM",
        "PN",
        "PR",
        "PS",
        "PT",
        "PW",
        "PY",
        "QA",
        "RE",
        "RO",
        "RS",
        "RU",
        "RW",
        "SA",
        "SB",
        "SC",
        "SD",
        "SE",
        "SG",
        "SH",
        "SI",
        "SJ",
        "SK",
        "SL",
        "SM",
        "SN",
        "SO",
        "SR",
        "SS",
        "ST",
        "SV",
        "SX",
        "SY",
        "SZ",
        "TC",
        "TD",
        "TF",
        "TG",
        "TH",
        "TJ",
        "TK",
        "TL",
        "TM",
        "TN",
        "TO",
        "TR",
        "TT",
        "TV",
        "TW",
        "TZ",
        "UA",
        "UG",
        "UM",
        "US",
        "UY",
        "UZ",
        "VA",
        "VC",
        "VE",
        "VG",
        "VI",
        "VN",
        "VU",
        "WF",
        "WS",
        "YE",
        "YT",
        "ZA",
        "ZM",
        "ZW"
      )
    }
  }

  "counts" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {

      val counts = VisibleTrackCounts(Some(111), Some(222), Some(333), Some(444), Some(555))
      val revealStatsTrack = defaultTrack.copy(revealStats = true, counts = counts)
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(
          visibleTrack = revealStatsTrack,
          loggedInUser = userUrn
        )
    }

    "adds counts" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "playback_count" ==== JsDefined(JsNumber(111))
      json \ "favoritings_count" ==== JsDefined(JsNumber(222))
      json \ "reposts_count" ==== JsDefined(JsNumber(333))
      json \ "download_count" ==== JsDefined(JsNumber(555))
    }
  }

  "comment counts" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {

      val showCommentsTrack = defaultTrack.copy(
        revealComments = true,
        revealStats = true,
        counts = VisibleTrackCounts(None, None, None, Some(444), None)
      )
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(
          visibleTrack = showCommentsTrack,
          loggedInUser = userUrn
        )
    }

    "adds counts when user is logged in and has reveal stats/comments" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "comment_count" ==== JsDefined(JsNumber(444))
    }
  }

  "user favourites" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {}

    "adds user favourite information when track is liked" in new Context {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack(isLiked = true)
      val json = Json.toJson(trackRepresentation)

      json \ "user_favorite" ==== JsDefined(JsBoolean(true))
    }

    "adds user favourite information when track is liked" in new Context {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack(isLiked = true)
      val json = Json.toJson(trackRepresentation)

      json \ "user_favorite" ==== JsDefined(JsBoolean(true))
    }

    "adds user favourite information when track is not liked" in new Context {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
      val json = Json.toJson(trackRepresentation)

      json \ "user_favorite" ==== JsDefined(JsBoolean(false))
    }
  }

  "user playback count" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
    }

    "adds a user playback count of 1" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "user_playback_count" ==== JsDefined(JsNumber(1))
    }
  }

  "duration" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
    }

    "returns the duration of the playable track" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "duration" ==== JsDefined(JsNumber(60000))
    }

    "replaces the track duration if snippet" in new Context {
      val json = Json.toJson(
        createTrackRepresentationFromVisibleTrack(
          visibleTrack = defaultTrack.copy(authorization = new ContentAuthorization(
            urn = trackUrn,
            policy = ContentPolicy.SNIP,
            reason = Reason.DEFAULT,
            contentRestriction = ContentRestriction.ENCRYPTED_STREAM_ONLY,
            monetizationModel = MonetizationModel.AD_SUPPORTED
          )
          )
        )
      )

      json \ "duration" ==== JsDefined(JsNumber(30000))
    }
  }

  "policy and monetization model" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(client = AllowlistedClients.clients.head)
    }

    "does not set policy and monetization model when not allowlisted" in new Context {
      val json = Json.toJson(createTrackRepresentationFromVisibleTrack())

      json \ "policy" ==== JsDefined(JsNull)
      json.as[JsObject].keys.contains("monetization_model") ==== false
    }

    "sets the policy when the client is allowlisted" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "policy" ==== JsDefined(JsString("MONETIZE"))
    }

    "sets the monetization model when the client is allowlisted" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "monetization_model" ==== JsDefined(JsString("AD_SUPPORTED"))
    }
  }

  "waveform url" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
    }

    "adds the PNG URL of the track's 'stream' waveform" in new Context {
      val json = Json.toJson(trackRepresentation)

      (json \ "waveform_url").as[String] ==== "https://bar.sndcdn.com/stream/a1b2c3.png"
    }
  }

  "private url" >> {
    "when all URL fields are present" >> {
      trait UrlsPresentContext extends Scope with TrackRepresentationSpecContext {

        val nonPublicTrack = defaultTrack.copy(public = false, secretToken = Some("bl3rkbi3"))
        val trackRepresentation: TrackRepresentation =
          createTrackRepresentationFromVisibleTrack(visibleTrack = nonPublicTrack)
        val json = Json.toJson(trackRepresentation)
      }

      "adds secret token stuff" in new UrlsPresentContext {
        json \ "secret_uri" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the URI" in new UrlsPresentContext {
        json \ "uri" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the stream_url" in new UrlsPresentContext {
        json \ "stream_url" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324/preview?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the download_url" in new UrlsPresentContext {
        json \ "download_url" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324/download?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the permalink_url" in new UrlsPresentContext {
        json \ "permalink_url" ==== JsDefined(
          JsString(
            "http://soundcloud.com/nirvana/plsty-remix/bl3rkbi3?utm_medium=api&utm_campaign=social_sharing&utm_source=id_123"
          )
        )
      }
    }

    "when a particular URL field has a badly-encoded secret token" >> {
      // Some clients do this, according to our logs
      "when a particular URL field has a badly-encoded secret token" >> {
        trait BadlyFormedSecretTokenContext extends Scope with TrackRepresentationSpecContext {

          val nonPublicTrack = defaultTrack.copy(public = false, secretToken = Some("badgers?format=json"))
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = nonPublicTrack,
              loggedInUser = userUrn
            )
          val json = Json.toJson(trackRepresentation)
        }

        "correctly encodes it into the URI" in new BadlyFormedSecretTokenContext {
          json \ "uri" ==== JsDefined(
            JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324?secret_token=badgers%3Fformat%3Djson")
          )
        }

        "correctly encodes it into the permalink_url" in new BadlyFormedSecretTokenContext {
          json \ "permalink_url" ==== JsDefined(
            JsString(
              "http://soundcloud.com/nirvana/plsty-remix/badgers%3Fformat%3Djson?utm_medium=api&utm_campaign=social_sharing&utm_source=id_123"
            )
          )
        }
      }
    }
  }

  "stream url" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      override def defaultTrack: VisibleTrack = super.defaultTrack.copy(access = Some(Access.Blocked))
    }

    "no stream url present when track is blocked" in new Context {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
      val json = Json.toJson(trackRepresentation)

      json \ "stream_url" ==== JsDefined(JsNull)
    }
  }

  "download url" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      override def defaultTrack: VisibleTrack = super.defaultTrack.copy(downloadable = false)
    }

    "no download url present when track is non-downloadable" in new Context {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
      val json = Json.toJson(trackRepresentation)

      json \ "download_url" ==== JsDefined(JsNull)
    }
  }

  "track representation from visible track all fields" >> {
    trait Context extends Scope with TrackRepresentationSpecContext

    "serialises to JSON correctly" in new Context {
      val trackJson = Json.toJson(createTrackRepresentationFromVisibleTrack())
      trackJson \ "kind" ==== JsDefined(JsString("track"))
      trackJson \ "id" ==== JsDefined(JsNumber(1324))
      trackJson \ "urn" ==== JsDefined(JsString("soundcloud:tracks:1324"))
      trackJson \ "created_at" ==== JsDefined(JsString("2015/02/15 16:47:27 +0000"))
      trackJson \ "duration" ==== JsDefined(JsNumber(60000))
      trackJson \ "commentable" ==== JsDefined(JsBoolean(false))
      trackJson \ "downloadable" ==== JsDefined(JsBoolean(true))
      trackJson \ "tag_list" ==== JsDefined(
        JsString("system:foo system:bar \"awesomeness:very high\" dubstep folk \"tag with spaces\"")
      )
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
      trackJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324"))
      trackJson \ "permalink_url" ==== JsDefined(
        JsString(
          "http://soundcloud.com/nirvana/plsty-remix?utm_medium=api&utm_campaign=social_sharing&utm_source=id_123"
        )
      )
      trackJson \ "stream_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324/preview")
      )
      trackJson \ "download_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:1324/download")
      )
      trackJson \ "purchase_url" ==== JsDefined(JsString("http://example.com/buy/7890"))
      trackJson \ "purchase_title" ==== JsDefined(JsString("buy me pls"))
      trackJson \ "bpm" ==== JsDefined(JsNumber(120.7))
      trackJson \ "release" ==== JsDefined(JsString("DR012"))
      trackJson \ "key_signature" ==== JsDefined(JsString("Emaj"))
      trackJson \ "access" ==== JsDefined(JsString("playable"))

      val userJson = trackJson \ "user"
      userJson \ "id" ==== JsDefined(JsNumber(3456))
      userJson \ "kind" ==== JsDefined(JsString("user"))
      userJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/soundcloud:users:3456"))
      userJson \ "permalink_url" ==== JsDefined(JsString("https://soundcloud.com/denis"))
      userJson \ "avatar_url" ==== JsDefined(JsString("https://example.com/giraffe.jpg"))
    }

    "streamable" >> {
      "true if api_streamable = true" in new Context {
        val trackJson =
          Json.toJson(
            createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(apiStreamable = Some(true)))
          )
        trackJson \ "streamable" ==== JsDefined(JsBoolean(true))
      }

      "false if api_streamable = false" in new Context {
        val trackJson =
          Json.toJson(
            createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(apiStreamable = Some(false)))
          )
        trackJson \ "streamable" ==== JsDefined(JsBoolean(false))
      }
    }

    "artwork_url" >> {
      "replaces original with large" in new Context {
        val artwork = Artwork(filename = Some("donkey-original.jpg"))
        val trackJson =
          Json.toJson(createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(artwork = artwork)))
        trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/donkey-large.jpg"))
      }

      "replaces png with jpg" in new Context {
        val artwork = Artwork(filename = Some("donkey-original.png"))
        val trackJson =
          Json.toJson(createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(artwork = artwork)))
        trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/donkey-large.jpg"))
      }

      "does not prefix cdn root when filename is already an absolute URL" in new Context {
        val fullUrl = "https://i1.sndcdn.com/artworks-vfcBYZmSWEj5dOIT-qAeFgQ-large.jpg"
        val artwork = Artwork(filename = Some(fullUrl))
        val trackJson =
          Json.toJson(createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(artwork = artwork)))
        trackJson \ "artwork_url" ==== JsDefined(JsString(fullUrl))
      }

      "rewrites -original to -large on absolute URL without duplicating host" in new Context {
        val artwork = Artwork(
          filename = Some("https://i1.sndcdn.com/artworks-vfcBYZmSWEj5dOIT-qAeFgQ-original.jpg")
        )
        val trackJson =
          Json.toJson(createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack.copy(artwork = artwork)))
        trackJson \ "artwork_url" ==== JsDefined(
          JsString("https://i1.sndcdn.com/artworks-vfcBYZmSWEj5dOIT-qAeFgQ-large.jpg")
        )
      }
    }

    "sharing" in new Context {
      val publicTrack = defaultTrack.copy(public = true)
      val publicTrackRepresentation = createTrackRepresentationFromVisibleTrack(visibleTrack = publicTrack)
      val publicTrackJson = Json.toJson(publicTrackRepresentation)
      publicTrackJson \ "sharing" ==== JsDefined(JsString("public"))

      val privateTrack = defaultTrack.copy(public = false)
      val privateTrackRepresentation = createTrackRepresentationFromVisibleTrack(visibleTrack = privateTrack)
      val privateTrackJson = Json.toJson(privateTrackRepresentation)
      privateTrackJson \ "sharing" ==== JsDefined(JsString("private"))
    }

    "strangely specific bpm values" in new Context {
      val visibleTrack = defaultTrack.copy(bpm = Some(128.10000610351562))
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "bpm" ==== JsDefined(JsNumber(128.1))
    }

    "release year, but no release day/month" in new Context {
      val visibleTrack = defaultTrack.copy(releaseYear = Some(2016), releaseMonth = None, releaseDay = None)
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "release_year" ==== JsDefined(JsNumber(2016))
      trackJson \ "release_month" ==== JsDefined(JsNumber(1))
      trackJson \ "release_day" ==== JsDefined(JsNumber(1))
    }

    "no release year" in new Context {
      val visibleTrack = defaultTrack.copy(releaseYear = None, releaseMonth = Some(2))
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "release_year" ==== JsDefined(JsNull)
      trackJson \ "release_month" ==== JsDefined(JsNull)
      trackJson \ "release_day" ==== JsDefined(JsNull)
    }

    "weird artwork filename" in new Context {
      val visibleTrack = defaultTrack.copy(artwork = Artwork(filename = Some("adfhlsh.jpg")))
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "artwork_url" ==== JsDefined(JsString("https://i1.sndcdn.com/adfhlsh.jpg"))
    }

    "user avatar URL with HTTP and no trailing number" in new Context {
      val user = defaultUser.copy(avatar_url = "http://example.com/img.png")
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack, user = user)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
    }

    "user avatar URL with HTTP and trailing number" in new Context {
      val user = defaultUser.copy(avatar_url = "http://example.com/img.png?123")
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack, user = user)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
    }

    "user avatar URL with HTTPS and trailing number" in new Context {
      val user = defaultUser.copy(avatar_url = "https://example.com/img.png?123")
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = defaultTrack, user = user)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "user" \ "avatar_url" ==== JsDefined(JsString("https://example.com/img.png"))
    }

    "no access" in new Context {
      val visibleTrack = defaultTrack.copy(access = None)
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "access" ==== JsDefined(JsNull)
    }
  }

  "track representation from track coordinator track all fields" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation = TrackRepresentationBuilder.fromTrackCoordinatorTrack(
        trackCoordinatorTrack = Fixtures.trackCoordinatorTrack.as[TrackCoordinatorTrack],
        user = defaultUser,
        agentUrn = Some(Urn("soundcloud", "applications", "123"))
      )
    }

    "serialises to JSON correctly" in new Context {
      val trackJson = Json.toJson(trackRepresentation)
      trackJson \ "kind" ==== JsDefined(JsString("track"))
      trackJson \ "id" ==== JsDefined(JsNumber(174088262))
      trackJson \ "created_at" ==== JsDefined(JsString("2014/10/27 16:25:25 +0000"))
      trackJson \ "duration" ==== JsDefined(JsNumber(0))
      trackJson \ "commentable" ==== JsDefined(JsBoolean(true))
      trackJson \ "tag_list" ==== JsDefined(JsString("tag onw two \"hello tag\" tōkyō"))
      trackJson \ "embeddable_by" ==== JsDefined(JsString("all"))
      trackJson \ "genre" ==== JsDefined(JsString("Free jazz"))
      trackJson \ "title" ==== JsDefined(JsString("Awesome Track"))
      trackJson \ "description" ==== JsDefined(JsString("This track is awesome"))
      trackJson \ "label_name" ==== JsDefined(JsString("Foobar records"))
      trackJson \ "isrc" ==== JsDefined(JsString("US-S1Z-99-00001"))
      trackJson \ "release_year" ==== JsDefined(JsNumber(2013))
      trackJson \ "release_month" ==== JsDefined(JsNumber(2))
      trackJson \ "release_day" ==== JsDefined(JsNumber(1))
      trackJson \ "license" ==== JsDefined(JsString("all-rights-reserved"))
      trackJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262"))
      trackJson \ "artwork_url" === JsDefined(JsString("https://i1.sndcdn.com/artworks-000095281756-51d163-large.jpg"))
      trackJson \ "permalink_url" ==== JsDefined(
        JsString(
          "https://soundcloud.com/imprisonedprecision/awesome-track-2014-10-27-17-25-29-66?utm_medium=api&utm_campaign=social_sharing&utm_source=id_123"
        )
      )
      trackJson \ "stream_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262/preview")
      )
      trackJson \ "download_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262/download")
      )
      trackJson \ "purchase_url" ==== JsDefined(JsString("http://buy.that.com"))
      trackJson \ "purchase_title" ==== JsDefined(JsString("buy123"))
      trackJson \ "bpm" ==== JsDefined(JsNumber(123.0))
      trackJson \ "release" ==== JsDefined(JsString("1234"))
      trackJson \ "key_signature" ==== JsDefined(JsString("A"))
      trackJson \ "access" ==== JsDefined(JsNull)

      val userJson = trackJson \ "user"
      userJson \ "id" ==== JsDefined(JsNumber(3456))
      userJson \ "kind" ==== JsDefined(JsString("user"))
      userJson \ "permalink" ==== JsDefined(JsString("giraffe"))
      userJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/soundcloud:users:3456"))
      userJson \ "permalink_url" ==== JsDefined(JsString("https://soundcloud.com/denis"))
      userJson \ "avatar_url" ==== JsDefined(JsString("https://example.com/giraffe.jpg"))
      userJson \ "last_modified" ==== JsDefined(JsString("2016/10/10 11:21:36 +0000"))
    }

    "add secret token to private tracks" in new Context {
      override val trackRepresentation = TrackRepresentationBuilder.fromTrackCoordinatorTrack(
        trackCoordinatorTrack = Fixtures.trackCoordinatorTrack.as[TrackCoordinatorTrack].copy(public = false),
        user = defaultUser,
        agentUrn = Some(Urn("soundcloud", "applications", "123"))
      )
      val trackJson = Json.toJson(trackRepresentation)

      trackJson \ "uri" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262?secret_token=s-8USae")
      )
      trackJson \ "secret_uri" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262?secret_token=s-8USae")
      )
      trackJson \ "permalink_url" ==== JsDefined(
        JsString(
          "https://soundcloud.com/imprisonedprecision/awesome-track-2014-10-27-17-25-29-66/s-8USae?utm_medium=api&utm_campaign=social_sharing&utm_source=id_123"
        )
      )
      trackJson \ "stream_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262/preview?secret_token=s-8USae")
      )
      trackJson \ "download_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:174088262/download?secret_token=s-8USae")
      )
    }
  }
}
