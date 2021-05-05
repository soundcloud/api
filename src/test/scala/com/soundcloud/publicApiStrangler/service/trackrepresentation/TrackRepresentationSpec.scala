package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.AllowlistedClients
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.media.TrackWaveformUrl
import com.soundcloud.publicApiStrangler.client.mothership.TrackAudioMetadata
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, UserRepresentation}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import com.soundcloud.publicApiStrangler.client.tracks.{
  Artwork,
  EmbeddingPermission,
  Transcoding,
  VisibleTrack,
  WaveformUrl
}
import com.soundcloud.publicApiStrangler.service.users.UserBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
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
      isrc: Option[Isrc] = defaultIsrc,
      counts: StitchCounts = defaultCounts,
      label: Option[UserRepresentation] = None,
      geoblockings: Geoblockings = defaultGeoblockings,
      audioMetadata: TrackAudioMetadata = defaultTrackAudioMetadata,
      downloadsPerTrack: Option[Int] = None,
      waveformUrl: TrackWaveformUrl = TrackWaveformUrl("some_uid", Url("https://bar.sndcdn.com/stream/a1b2c3.png")),
      isLiked: Boolean = false
  ) = {
    TrackRepresentationBuilder.fromVisibleTrack(
      client = Some(client),
      sessionUser = Some(loggedInUser),
      visibleTrack = visibleTrack,
      user = user,
      isrc = isrc,
      counts = counts,
      label = label,
      geoblockings = geoblockings,
      trackAudioMetadata = audioMetadata,
      isLiked = isLiked,
      waveformUrl = waveformUrl,
      downloadsPerTrack = downloadsPerTrack
    )
  }

  def defaultLoggedInUserUrn = Urn("soundcloud", "users", "79241")

  def defaultTrackAudioMetadata =
    TrackAudioMetadata(
      state = "finished",
      original_format = Some("vqf"),
      original_content_size = Some(9001)
    )

  def defaultUser = new UserBuilder().setUrn(userUrn).build
  def defaultLabelUrn = Some(labelUrn)

  def defaultLabel: UserRepresentation = new UserBuilder().setUrn(labelUrn).build

  def defaultTrack = VisibleTrack(
    urn = trackUrn,
    userUrn = userUrn,
    commentable = false,
    description = Some("Follow @samstarling !"),
    createdAt = new LocalDateTime(2015, 2, 15, 16, 47, 27),
    disabledAt = None,
    downloadable = false,
    duration = 60000,
    genre = Some("future bass"),
    lastModified = new LocalDateTime(2016, 8, 8, 13, 28, 53),
    permalink = "plsty-remix",
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
    trackType = Some("original"),
    release = Some("DR012"),
    keySignature = Some("Emaj"),
    videoUrl = Some("http://example.com/video.mp4"),
    labelId = defaultLabelUrn.map(_.identifier.toLong),
    supplyChainStatus = Some("manual_upload"),
    authorization = new ContentAuthorization(
      trackUrn,
      ContentPolicy.MONETIZE,
      Reason.NOT_SUPPORTED,
      ContentRestriction.ENCRYPTED_STREAM_ONLY,
      MonetizationModel.AD_SUPPORTED
    ),
    transcodings = List.empty[Transcoding],
    waveformUrls = List.empty[WaveformUrl],
    access = Some(Access.Playable)
  )

  def defaultIsrc = Some(Isrc("US-S1Z-99-00001"))

  def defaultCounts = StitchCounts(111, 222, 333, 444, 555)

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

  "labels" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {

      val label: UserRepresentation = defaultLabel
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack(label = Some(label))
    }

    "adds label info" in new Context {
      val json = Json.toJson(trackRepresentation)

      val labelJson = json \ "label"
      labelJson \ "id" ==== JsDefined(JsNumber(999))
      labelJson \ "kind" ==== JsDefined(JsString("user"))
      labelJson \ "permalink" ==== JsDefined(JsString("giraffe"))
      labelJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/999"))
      labelJson \ "permalink_url" ==== JsDefined(JsString("https://soundcloud.com/denis"))
      labelJson \ "avatar_url" ==== JsDefined(JsString("https://example.com/giraffe.jpg"))
    }
  }

  "counts" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {

      val revealStatsTrack = defaultTrack.copy(revealStats = true)
      val counts = StitchCounts(111, 222, 333, 444, 555)
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(
          visibleTrack = revealStatsTrack,
          counts = counts,
          loggedInUser = userUrn
        )
    }

    "adds counts" in new Context {
      val json = Json.toJson(trackRepresentation)

      json \ "playback_count" ==== JsDefined(JsNumber(111))
      json \ "download_count" ==== JsDefined(JsNumber(222))
      json \ "favoritings_count" ==== JsDefined(JsNumber(333))
      json \ "reposts_count" ==== JsDefined(JsNumber(555))
    }
  }

  "comment counts" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {

      val showCommentsTrack = defaultTrack.copy(revealComments = true, revealStats = true)
      val counts = StitchCounts(1, 2, 3, 444, 555)
      val trackRepresentation: TrackRepresentation =
        createTrackRepresentationFromVisibleTrack(
          visibleTrack = showCommentsTrack,
          counts = counts,
          loggedInUser = userUrn
        )
    }

    "adds counts" in new Context {
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

  "domain lockings" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val trackRepresentation: TrackRepresentation = createTrackRepresentationFromVisibleTrack()
    }

    "are null" in new Context {
      val json = Json.toJson(trackRepresentation)
      json.as[JsObject].value("domain_lockings") === JsNull
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
      json \ "monetization_model" ==== JsDefined(JsNull)
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
        json \ "secret_token" ==== JsDefined(JsString("bl3rkbi3"))
        json \ "secret_uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324?secret_token=bl3rkbi3"))
      }

      "adds the secret token to the URI" in new UrlsPresentContext {
        json \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/1324?secret_token=bl3rkbi3"))
      }

      "adds the secret token to the stream_url" in new UrlsPresentContext {
        json \ "stream_url" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/1324/stream?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the download_url" in new UrlsPresentContext {
        json \ "download_url" ==== JsDefined(
          JsString("https://api.soundcloud.com/tracks/1324/download?secret_token=bl3rkbi3")
        )
      }

      "adds the secret token to the permalink_url" in new UrlsPresentContext {
        json \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/nirvana/plsty-remix/bl3rkbi3"))
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
            JsString("https://api.soundcloud.com/tracks/1324?secret_token=badgers%3Fformat%3Djson")
          )
        }

        "correctly encodes it into the permalink_url" in new BadlyFormedSecretTokenContext {
          json \ "permalink_url" ==== JsDefined(
            JsString("http://soundcloud.com/nirvana/plsty-remix/badgers%3Fformat%3Djson")
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

  "quota counts" >> {
    trait Context extends Scope with TrackRepresentationSpecContext {
      val ninetyDownloads = StitchCounts(
        download_count = 90,
        playback_count = 0,
        favoritings_count = 0,
        comment_count = 0,
        reposts_count = 0
      )

      val oneHundredDownloads = StitchCounts(
        download_count = 100,
        playback_count = 0,
        favoritings_count = 0,
        comment_count = 0,
        reposts_count = 0
      )

      val oneThousandDownloads = StitchCounts(
        download_count = 1000,
        playback_count = 0,
        favoritings_count = 0,
        comment_count = 0,
        reposts_count = 0
      )
    }

    "downloadable" >> {
      "when the track is downloadable" >> {
        "adds downloadable as true when downloads are below the user's quota" in new Context {
          val trackRepresentation: TrackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = ninetyDownloads
            )

          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(true))
        }

        "adds downloadable as false when downloads are exactly at the user's quota" in new Context {
          val trackRepresentation: TrackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = oneHundredDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }

        "adds downloadable as false when downloads are above the user's quota" in new Context {
          val trackRepresentation: TrackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = oneThousandDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }

        "adds downloadable as true when the user has no quota (ie. unlimited)" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = None,
              counts = oneThousandDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(true))
        }
      }

      "when the track is not downloadable" >> {
        "adds downloadable as false when downloads are below the user's quota" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              downloadsPerTrack = Some(100),
              counts = ninetyDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }

        "adds downloadable as false when the user has no quota (ie. unlimited)" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(downloadsPerTrack = None, counts = ninetyDownloads)
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }
      }

      "when the track has a 'downloadable' value that is null (None)" >> {
        "adds downloadable as false when downloads are below the user's quota" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = false),
              downloadsPerTrack = Some(100),
              counts = ninetyDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }

        "adds downloadable as false when the user has no quota (ie. unlimited)" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = false),
              downloadsPerTrack = None,
              counts = ninetyDownloads
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloadable" ==== JsDefined(JsBoolean(false))
        }
      }
    }

    "downloads_remaining" >> {
      "when requesting user is the owner" >> {
        "adds number of downloads remaining, if the user has a quota" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = ninetyDownloads,
              loggedInUser = userUrn
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloads_remaining" ==== JsDefined(JsNumber(10))
        }

        "adds number of downloads remaining, even if track has no downloads remaining" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = oneHundredDownloads,
              loggedInUser = userUrn
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloads_remaining" ==== JsDefined(JsNumber(0))
        }

        "adds number of downloads remaining, even if track is not downloadable" in new Context {
          val trackRepresentation =
            createTrackRepresentationFromVisibleTrack(
              visibleTrack = defaultTrack.copy(downloadable = true),
              downloadsPerTrack = Some(100),
              counts = oneHundredDownloads,
              loggedInUser = userUrn
            )
          val json = Json.toJson(trackRepresentation)
          json \ "downloads_remaining" ==== JsDefined(JsNumber(0))
        }

        "when requesting user is not the owner" >> {
          "does not add number of downloads remaining, if the user has a quota" in new Context {
            val trackRepresentation =
              createTrackRepresentationFromVisibleTrack(
                visibleTrack = defaultTrack.copy(downloadable = true),
                downloadsPerTrack = Some(100),
                counts = ninetyDownloads
              )
            val json = Json.toJson(trackRepresentation)
            json.as[JsObject].value("downloads_remaining") === JsNull
          }

          "does not add number of downloads remaining, if the user has no quota (ie. unlimited)" in new Context {
            val trackRepresentation =
              createTrackRepresentationFromVisibleTrack(
                visibleTrack = defaultTrack.copy(downloadable = true),
                downloadsPerTrack = None,
                counts = ninetyDownloads,
                loggedInUser = userUrn
              )
            val json = Json.toJson(trackRepresentation)
            json.as[JsObject].value("downloads_remaining") === JsNull
          }
        }
      }
    }
  }

  "track representation from visible track all fields" >> {
    trait Context extends Scope with TrackRepresentationSpecContext

    "serialises to JSON correctly" in new Context {
      val trackJson = Json.toJson(createTrackRepresentationFromVisibleTrack())
      trackJson \ "kind" ==== JsDefined(JsString("track"))
      trackJson \ "id" ==== JsDefined(JsNumber(1324))
      trackJson \ "created_at" ==== JsDefined(JsString("2015/02/15 16:47:27 +0000"))
      trackJson \ "user_id" ==== JsDefined(JsNumber(3456))
      trackJson \ "duration" ==== JsDefined(JsNumber(60000))
      trackJson \ "commentable" ==== JsDefined(JsBoolean(false))
      trackJson \ "last_modified" ==== JsDefined(JsString("2016/08/08 13:28:53 +0000"))
      trackJson \ "tag_list" ==== JsDefined(
        JsString("system:foo system:bar \"awesomeness:very high\" dubstep folk \"tag with spaces\"")
      )
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
      trackJson \ "access" ==== JsDefined(JsString("playable"))

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

    "sanitizes string values" in new Context {
      val visibleTrack = defaultTrack.copy(
        purchaseTitle = Some("<script></script>bla"),
        genre = Some("<script></script>bla"),
        title = "<script></script>bla",
        description = Some("<script></script>bla"),
        labelName = Some("<script></script>bla"),
        release = Some("<script></script>bla"),
        trackType = Some("<script></script>bla"),
        keySignature = Some("<script></script>bla")
      )
      val trackRep = createTrackRepresentationFromVisibleTrack(visibleTrack = visibleTrack)
      val trackJson = Json.toJson(trackRep)
      trackJson \ "purchase_title" ==== JsDefined(JsString("bla"))
      trackJson \ "genre" ==== JsDefined(JsString("bla"))
      trackJson \ "title" ==== JsDefined(JsString("bla"))
      trackJson \ "description" ==== JsDefined(JsString("bla"))
      trackJson \ "label_name" ==== JsDefined(JsString("bla"))
      trackJson \ "release" ==== JsDefined(JsString("bla"))
      trackJson \ "track_type" ==== JsDefined(JsString("bla"))
      trackJson \ "key_signature" ==== JsDefined(JsString("bla"))
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
      trackJson \ "user_id" ==== JsDefined(JsNumber(3456))
      trackJson \ "duration" ==== JsDefined(JsNumber(0))
      trackJson \ "commentable" ==== JsDefined(JsBoolean(true))
      trackJson \ "last_modified" ==== JsDefined(JsString("2014/10/27 16:25:25 +0000"))
      trackJson \ "tag_list" ==== JsDefined(JsString("tag onw two \"hello tag\" tōkyō"))
      trackJson \ "permalink" ==== JsDefined(JsString("awesome-track-2014-10-27-17-25-29-66"))
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
      trackJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/174088262"))
      trackJson \ "artwork_url" === JsDefined(JsString("https://i1.sndcdn.com/artworks-000095281756-51d163-large.jpg"))
      trackJson \ "permalink_url" ==== JsDefined(
        JsString("https://soundcloud.com/imprisonedprecision/awesome-track-2014-10-27-17-25-29-66")
      )
      trackJson \ "stream_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/174088262/stream"))
      trackJson \ "download_url" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/174088262/download"))
      trackJson \ "purchase_url" ==== JsDefined(JsString("http://buy.that.com"))
      trackJson \ "purchase_title" ==== JsDefined(JsString("buy123"))
      trackJson \ "bpm" ==== JsDefined(JsNumber(123.0))
      trackJson \ "track_type" ==== JsDefined(JsNull)
      trackJson \ "release" ==== JsDefined(JsString("1234"))
      trackJson \ "key_signature" ==== JsDefined(JsString("A"))
      trackJson \ "video_url" ==== JsDefined(JsNull)
      trackJson \ "label_id" ==== JsDefined(JsNull)
      trackJson \ "state" ==== JsDefined(JsString("storing"))
      trackJson \ "original_format" ==== JsDefined(JsString("mp3"))
      trackJson \ "original_content_size" ==== JsDefined(JsNumber(6923))
      trackJson \ "access" ==== JsDefined(JsNull)

      val userJson = trackJson \ "user"
      userJson \ "id" ==== JsDefined(JsNumber(3456))
      userJson \ "kind" ==== JsDefined(JsString("user"))
      userJson \ "permalink" ==== JsDefined(JsString("giraffe"))
      userJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/users/3456"))
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

      trackJson \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/174088262?secret_token=s-8USae"))
      trackJson \ "secret_uri" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/174088262?secret_token=s-8USae")
      )
      trackJson \ "permalink_url" ==== JsDefined(
        JsString("https://soundcloud.com/imprisonedprecision/awesome-track-2014-10-27-17-25-29-66/s-8USae")
      )
      trackJson \ "stream_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/174088262/stream?secret_token=s-8USae")
      )
      trackJson \ "download_url" ==== JsDefined(
        JsString("https://api.soundcloud.com/tracks/174088262/download?secret_token=s-8USae")
      )
    }
  }
}
