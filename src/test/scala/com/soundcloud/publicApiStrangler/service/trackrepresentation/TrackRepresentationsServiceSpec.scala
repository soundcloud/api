package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.jvmkit.module.util.{Url, Urn}
import com.soundcloud.publicApiStrangler.authorization.policies.{
  ContentAuthorization,
  ContentPolicy,
  ContentRestriction,
  MonetizationModel,
  Reason
}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.{TrackWaveformUrl, WaveformUrlsGenerator}
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{Geoblockings, User}
import com.soundcloud.publicApiStrangler.client.mothership.{DomainLocking, RichOkidokiClient, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.{Isrc, PubmeseClient}
import com.soundcloud.publicApiStrangler.client.quota.UserQuotaClient
import com.soundcloud.publicApiStrangler.client.stitch.{StitchClient, StitchCounts}
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission, TrackmetadataClient}
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import org.mockito.Mockito._
import play.api.libs.json._

class TrackRepresentationsServiceSpec extends UnitSpecification {
  trait Context extends Scope {
    implicit val trackRepresentationWrites = TrackRepresentation.writes

    val trackVisibilityService = mock[TrackVisibilityService]
    val okidokiClient = mock[RichOkidokiClient]
    val pubmeseClient = mock[PubmeseClient]
    val stitchClient = mock[StitchClient]
    val lieblingClient = mock[LieblingClient]
    val waveformUrlsGenerator = mock[WaveformUrlsGenerator]
    val userQuotaClient = mock[UserQuotaClient]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackPagination = mock[TrackPagination]

    val tracksService = new TrackRepresentationsService(
      trackVisibilityService,
      okidokiClient,
      pubmeseClient,
      stitchClient,
      lieblingClient,
      waveformUrlsGenerator,
      userQuotaClient,
      trackmetadataClient
    )

    val requestingUserUrn = Urn("soundcloud", "users", "112")
    val labelUrn = Urn("soundcloud", "users", "678")
    val trackOwnerUrn = Urn("soundcloud", "users", "3000")

    def trackOwner =
      User(
        urn = trackOwnerUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    def requestingUser =
      User(
        urn = requestingUserUrn,
        permalink = "giraffe",
        username = "Dr. G. Raffe",
        avatar_url = "http://example.com/giraffe.jpg",
        permalink_url = "http://soundcloud.com/denis",
        city = None,
        country = None,
        tracks_count = 1,
        followers_count = Some(20000),
        followings_count = Some(20),
        verified = false,
        description = Some("I am a nice person"),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    def label =
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
        description = Some("Psychonaut Music Inc."),
        updated_at = Some("2016/10/10 11:21:36 +0000")
      )

    val trackUrn = Urn("soundcloud", "tracks", "987")
    val trackRequest = TrackRequest(trackUrn, None)
    val createdAt = new LocalDateTime(2016, 5, 19, 18, 3, 4)
    val lastModified = new LocalDateTime(2016, 5, 20, 18, 3, 4)
    val authorization = new ContentAuthorization(
      trackUrn,
      ContentPolicy.MONETIZE,
      Reason.NOT_SUPPORTED,
      ContentRestriction.ENCRYPTED_STREAM_ONLY,
      MonetizationModel.AD_SUPPORTED
    )

    def geoblockings: Map[Urn, Geoblockings] = Map(trackUrn -> List("DE", "FR"))

    def domainLockings: Map[Urn, List[DomainLocking]] =
      Map(
        trackUrn ->
          List(
            DomainLocking(
              domain = "example.com",
              urn = Urn("soundcloud", "domain-lockings", "1"),
              trackUrn = Urn("soundcloud", "tracks", "123")
            )
          )
      )

    def trackAudioMetadata: Map[Urn, TrackAudioMetadata] =
      Map(
        trackUrn ->
          TrackAudioMetadata(
            state = "failed",
            original_content_size = Some(9001),
            original_format = Some("vqf")
          )
      )

    def trackvisibilityTrack(
        disabledAt: Option[LocalDateTime] = None,
        isPublic: Boolean = true,
        secretToken: String = "secr3t-Token",
        isDownloadable: Boolean = false,
        user: Urn = trackOwnerUrn,
        labelId: Option[Long] = Some(labelUrn.identifier.toLong),
        revealStats: Boolean = false,
        revealComments: Boolean = true
    ) =
      VisibleTrack(
        urn = trackUrn,
        userUrn = user,
        commentable = false,
        description = None,
        createdAt = createdAt,
        disabledAt = disabledAt,
        downloadable = isDownloadable,
        duration = 0,
        genre = None,
        lastModified = lastModified,
        permalink = null,
        permalinkUrl = None,
        public = isPublic,
        secretToken = Some(secretToken),
        userTags = List.empty,
        machineTags = List.empty,
        title = null,
        uid = Some("a1b2c3"),
        apiStreamable = None,
        streamable = true,
        revealStats = revealStats,
        revealComments = revealComments,
        labelName = None,
        license = null,
        embeddable = None,
        releaseYear = None,
        releaseMonth = None,
        releaseDay = None,
        embeddableBy = EmbeddingPermission.None,
        releaseDate = None,
        artwork = Artwork(None),
        publishedAt = None,
        purchaseUrl = Some("http://example.com/buy/7890"),
        purchaseTitle = Some("buy me pls"),
        bpm = Some(120.7),
        trackType = Some("original"),
        release = Some("DR012"),
        keySignature = Some("Emaj"),
        videoUrl = Some("http://example.com/video.mp4"),
        labelId = labelId,
        supplyChainStatus = None,
        waveformUrls = List.empty,
        transcodings = List.empty,
        authorization = authorization
      )

    def isrc(wrapped: String = "US-S1Z-99-00001"): Map[Urn, Isrc] =
      Map(trackUrn -> Isrc(wrapped))

    def stitchCounts: Map[Urn, StitchCounts] =
      Map(trackUrn -> StitchCounts(111, 222, 333, 444, 555))

    def userLikedTracks: Map[Urn, Boolean] =
      Map(trackUrn -> true)

    def waveformUrl(uid: String) =
      TrackWaveformUrl(uid, Url("https://bar.sndcdn.com/stream/a1b2c3.png"))

    val session: UserSession = new UserSessionBuilder().setUser(requestingUserUrn).build()

    def setUpMocksForExistingTrack(
        track: VisibleTrack,
        session: UserSession
    ) = {
      when(trackVisibilityService.tracks(session, List(trackRequest)))
        .thenReturn(Future.value(List(track)))
      when(okidokiClient.fetchUserObjects(session, Set(requestingUserUrn)))
        .thenReturn(Future.value(List(requestingUser)))
      when(okidokiClient.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(label)))
      when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.value(List(trackOwner)))
      when(pubmeseClient.isrcsForTracks(session, Set(trackUrn))).thenReturn(Future.value(isrc()))
      when(stitchClient.countsForTracks(session, Set((trackOwnerUrn, trackUrn)))).thenReturn(Future.value(stitchCounts))
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn))).thenReturn(Future.value(geoblockings))
      when(okidokiClient.fetchTracksDomainLockings(session, Set(trackUrn))).thenReturn(Future.value(domainLockings))
      when(okidokiClient.fetchTracksAudioMetadata(session, Set(trackUrn)))
        .thenReturn(Future.value(trackAudioMetadata))
      when(lieblingClient.userLikedTracks(session, Set(trackUrn), session.getUser))
        .thenReturn(Future.value(userLikedTracks))
      when(waveformUrlsGenerator.fromUid(track.uid.get)).thenReturn(waveformUrl(track.uid.get))
      when(okidokiClient.fetchUsersMap(session, Set(labelUrn)))
        .thenReturn(Future.value(Map(labelUrn -> trackOwner)))
      when(userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)))
        .thenReturn(Future.value(Map.empty[Urn, Option[Int]]))
    }

    def setUpMocksForNonExistingTrack = {
      when(trackVisibilityService.tracks(session, List(trackRequest))).thenReturn(Future.value(List.empty))
      when(pubmeseClient.isrcsForTracks(session, Set(trackUrn))).thenReturn(Future.value(isrc()))
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn))).thenReturn(Future.value(geoblockings))
      when(okidokiClient.fetchTracksDomainLockings(session, Set(trackUrn))).thenReturn(Future.value(domainLockings))
      when(okidokiClient.fetchTracksAudioMetadata(session, Set(trackUrn)))
        .thenReturn(Future.value(trackAudioMetadata))
    }

    def setUpMocksForMultipleExistingTracks(
        track: VisibleTrack,
        session: UserSession
    ) = {
      setUpMocksForExistingTrack(track, session)
      when(trackmetadataClient.urnsByUser(session, trackOwnerUrn)).thenReturn(Future.value(List(trackUrn)))
      when(trackPagination.calculateTrackUrnPage(List(trackUrn))).thenReturn(Set(trackUrn))
      when(trackPagination.calculateFinalPage(List(track))).thenReturn(List(track))
    }
  }

  "#tracks" >> {

    "Returns Some(x) for public tracks" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))

      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentationLike]
        case None =>
      }
    }

    "Returns Some(x) for private tracks if the owner is requesting" in new Context {
      val track = trackvisibilityTrack(isPublic = false)
      val ownerSession = new UserSessionBuilder().setUser(trackOwnerUrn).build
      setUpMocksForExistingTrack(track, ownerSession)

      val trackRepLike = Await.result(tracksService.track(ownerSession, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentationLike]
        case None =>
      }
    }

    "Returns Some(x) for private tracks if there is a correct secret token" in new Context {
      val correctSecretToken = "aSecre_t"
      val track = trackvisibilityTrack(isPublic = false, secretToken = correctSecretToken)
      override val trackRequest = TrackRequest(trackUrn, Some(correctSecretToken))
      setUpMocksForExistingTrack(track, session)

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentationLike]
        case None =>
      }
    }

    "Returns null ISRC when Pubmese is failing" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(pubmeseClient.isrcsForTracks(session, Set(trackUrn)))
        .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep) \ "isrc" ==== JsDefined(JsNull)
        case None =>
      }
    }

    "Returns no geoblockings if Moshimoshi is failing" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn)))
        .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep).as[JsObject].keys.contains("available_country_codes") ==== false
        case None =>
      }
    }

    "Returns no geoblockings if Moshimoshi returns an empty list" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn)))
        .thenReturn(Future.value(Map(trackUrn -> List())))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep).as[JsObject].keys.contains("available_country_codes") ==== false
        case None =>
      }
    }

    "Returns no domainlockings if Moshimoshi is failing" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTracksDomainLockings(session, Set(trackUrn)))
        .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep).as[JsObject].keys.contains("domain_lockings") ==== false
        case None =>
      }
    }

    "Returns Success when partial audio metadata for a track is unavailable" in new Context {
      val track = trackvisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTracksAudioMetadata(session, Set(trackUrn)))
        .thenReturn(Future.value(Map(trackUrn -> TrackAudioMetadata("storing", None, None))))
      val trackRepLike = Await.result(tracksService.track(session, trackRequest))

      trackRepLike match {
        case Some(rep) =>
          val audioMetadata = Json.toJson(rep)
          audioMetadata \ "state" ==== JsDefined(JsString("storing"))
          audioMetadata \ "original_content_size" ==== JsDefined(JsNull)
          audioMetadata \ "original_format" ==== JsDefined(JsNull)
        case None =>
      }
    }

    "user_favorite" >> {
      "is true when the user has favourited the track, and is logged in" in new Context {
        val track = trackvisibilityTrack()
        override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep) \ "user_favorite" ==== JsDefined(JsBoolean(true))
          case None =>
        }
      }

      "is false when the user has not favourited the track, and is logged in" in new Context {
        val track = trackvisibilityTrack()
        override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
        setUpMocksForExistingTrack(track, session)
        when(lieblingClient.userLikedTracks(session, Set(trackUrn), session.getUser))
          .thenReturn(Future.value(Map(trackUrn -> false)))

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep) \ "user_favorite" ==== JsDefined(JsBoolean(false))
          case None =>
        }
      }

      "is not present when the user is not logged in" in new Context {
        val track = trackvisibilityTrack()
        override val session = anonymousSession
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("user_favorite") ==== false
          case None =>
        }
      }
    }

    "user_playback_count" >> {
      "is always 1 when the user is logged in" in new Context {
        val track = trackvisibilityTrack()
        override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep) \ "user_playback_count" ==== JsDefined(JsNumber(1))
          case None =>
        }
      }

      "is not present when the user is not logged in" in new Context {
        val track = trackvisibilityTrack()
        override val session = anonymousSession
        session.isAnonymous ==== true
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("user_playback_count") ==== false
          case None =>
        }
      }
    }

    "waveform_url" >> {
      "is present when urlgen returns a stream URL" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("waveform_url") ==== true
          case None =>
        }
      }
    }

    "label" >> {
      "is present when track has a label" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForExistingTrack(track, session)
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("label") ==== true
          case None =>
        }
      }

      "is not present when track has not a label" in new Context {
        val track = trackvisibilityTrack(labelId = None)
        setUpMocksForExistingTrack(track, session)
        when(okidokiClient.fetchUsersMap(session, Set.empty)).thenReturn(Future.value(Map.empty[Urn, User]))
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("label") ==== false
          case None =>
        }
      }
    }

    "private urls" >> {
      "track token in params is correct" >> {
        "appends the track secret token to urls" in new Context {
          val track = trackvisibilityTrack().copy(permalinkUrl = Some("http://soundcloud.com/foo/bar"), public = false)
          override val trackRequest = TrackRequest(trackUrn, Some("secr3t-Token"))
          setUpMocksForExistingTrack(track, session)

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "uri" ==== JsDefined(JsString("https://api.soundcloud.com/tracks/987?secret_token=secr3t-Token"))
              json \ "stream_url" ==== JsDefined(
                JsString("https://api.soundcloud.com/tracks/987/stream?secret_token=secr3t-Token")
              )
              json \ "download_url" ==== JsDefined(
                JsString("https://api.soundcloud.com/tracks/987/download?secret_token=secr3t-Token")
              )
              json \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/foo/bar/secr3t-Token"))
            case None =>
          }
        }

        "does not add a secret token to null values" in new Context {
          val track = trackvisibilityTrack().copy(permalinkUrl = None)
          override val trackRequest = TrackRequest(trackUrn, Some("s-4kT0a"))
          setUpMocksForExistingTrack(track, session)

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "permalink_url" ==== JsDefined(JsNull)
            case None =>
          }
        }
      }
    }

    "downloadable" >> {
      "is true when track is downloadable, and below user's quota" in new Context {
        val track = trackvisibilityTrack(isDownloadable = true)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
          Map(track.userUrn -> Some(100))
        )
        stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
          Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
        )
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            val json = Json.toJson(rep)
            json \ "downloadable" ==== JsDefined(JsBoolean(true))
          case None =>
        }
      }

      "it true when track is downloadable, and use has no quota" in new Context {
        val track = trackvisibilityTrack(isDownloadable = true)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(Map(track.userUrn -> None))
        stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
          Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
        )
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            val json = Json.toJson(rep)
            json \ "downloadable" ==== JsDefined(JsBoolean(true))
          case None =>
        }
      }

      "is false when track is downloadable, and above user's quota" in new Context {
        val track = trackvisibilityTrack(isDownloadable = true)
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
          Map(track.userUrn -> Some(100))
        )
        stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
          Map(trackUrn -> StitchCounts(0, 100, 0, 0, 0))
        )
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            val json = Json.toJson(rep)
            json \ "downloadable" ==== JsDefined(JsBoolean(false))
          case None =>
        }
      }

      "is false when track is not downloadable, and below user's quota" in new Context {
        val track = trackvisibilityTrack()
        setUpMocksForExistingTrack(track, session)
        userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
          Map(track.userUrn -> Some(100))
        )
        stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
          Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
        )
        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            val json = Json.toJson(rep)
            json \ "downloadable" ==== JsDefined(JsBoolean(false))
          case None =>
        }
      }
    }

    "downloads_remaining" >> {
      "when the requesting user is not the owner of the track" >> {
        "it is not shown" in new Context {
          val track = trackvisibilityTrack(isDownloadable = true)
          setUpMocksForExistingTrack(track, session)
          userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
            Map(track.userUrn -> Some(100))
          )
          stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
            Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
          )

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              Json.toJson(rep).as[JsObject].keys.contains("downloads_remaining") ==== false
            case None =>
          }
        }
      }

      "when the requesting user is the owner of the track" >> {
        "is shown when track is below quota" in new Context {
          val track = trackvisibilityTrack(isDownloadable = true, user = session.getUser)
          setUpMocksForExistingTrack(track, session)
          when(userQuotaClient.downloadsPerTrack(session, Set(track.userUrn))).thenReturn(
            Future.value(
              Map(session.getUser -> Some(100))
            )
          )
          when(stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn)))).thenReturn(
            Future.value(
              Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
            )
          )
          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "downloads_remaining" ==== JsDefined(JsNumber(10))
            case None =>
          }
        }

        "it not shown when the track's user has no quota (eg. is unlimited)" in new Context {
          val track = trackvisibilityTrack(isDownloadable = true, user = session.getUser)
          setUpMocksForExistingTrack(track, session)
          userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
            Map(track.userUrn -> None)
          )
          stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
            Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
          )

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              Json.toJson(rep).as[JsObject].keys.contains("downloads_remaining") ==== false
            case None =>
          }
        }

        "is shown when no downloads remain" in new Context {
          val track = trackvisibilityTrack(isDownloadable = true, user = session.getUser)
          setUpMocksForExistingTrack(track, session)
          userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
            Map(track.userUrn -> Some(100))
          )
          stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
            Map(trackUrn -> StitchCounts(0, 100, 0, 0, 0))
          )
          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "downloads_remaining" ==== JsDefined(JsNumber(0))
            case None =>
          }
        }

        "is shown even if track is not downloadable" in new Context {
          val track = trackvisibilityTrack(user = session.getUser)
          setUpMocksForExistingTrack(track, session)
          userQuotaClient.downloadsPerTrack(session, Set(track.userUrn)) returns Future.value(
            Map(track.userUrn -> Some(100))
          )
          stitchClient.countsForTracks(session, Set((track.userUrn, trackUrn))) returns Future.value(
            Map(trackUrn -> StitchCounts(0, 90, 0, 0, 0))
          )

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "downloads_remaining" ==== JsDefined(JsNumber(10))
            case None =>
          }
        }
      }
    }

    "counts" >> {
      "requesting as uploader" >> {
        "returns proper counts" in new Context {
          override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

          val track = trackvisibilityTrack()
          setUpMocksForExistingTrack(track, session)

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))

          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "playback_count" ==== JsDefined(JsNumber(111))
              json \ "download_count" ==== JsDefined(JsNumber(222))
              json \ "favoritings_count" ==== JsDefined(JsNumber(333))
              json \ "reposts_count" ==== JsDefined(JsNumber(555))
            case None =>
          }
        }

        "returns empty counts if Stitch is failing" in new Context {
          override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

          val track = trackvisibilityTrack()
          setUpMocksForExistingTrack(track, session)
          when(stitchClient.countsForTracks(session, Set((trackOwnerUrn, trackUrn))))
            .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))

          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "playback_count" ==== JsDefined(JsNumber(0))
              json \ "download_count" ==== JsDefined(JsNumber(0))
              json \ "favoritings_count" ==== JsDefined(JsNumber(0))
              json \ "comment_count" ==== JsDefined(JsNumber(0))
            case None =>
          }
        }

        "includes comment_count if reveal_comments = true" in new Context {
          override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

          val track = trackvisibilityTrack()
          setUpMocksForExistingTrack(track, session)

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))

          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "comment_count" ==== JsDefined(JsNumber(444))
            case None =>
          }
        }

        "does not include comment_count if reveal_comments = false" in new Context {
          override val session = new UserSessionBuilder().setUser(trackOwnerUrn).build

          val track = trackvisibilityTrack(revealComments = false)
          setUpMocksForExistingTrack(track, session)

          val trackRepLike = Await.result(tracksService.track(session, trackRequest))

          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json.as[JsObject].keys.contains("comment_count") ==== false
            case None =>
          }
        }
      }

      "not requesting as uploader" >> {
        "track stats are not public" >> {
          "returns no counts" in new Context {
            val track = trackvisibilityTrack()
            setUpMocksForExistingTrack(track, session)

            val trackRepLike = Await.result(tracksService.track(session, trackRequest))

            trackRepLike match {
              case Some(rep) =>
                val json = Json.toJson(rep)
                json.as[JsObject].keys.contains("playback_count") ==== false
                json.as[JsObject].keys.contains("download_count") ==== false
                json.as[JsObject].keys.contains("favoritings_count") ==== false
                json.as[JsObject].keys.contains("comment_count") ==== false
              case None =>
            }
          }
        }

        "track has public stats" >> {
          "returns proper counts" in new Context {
            val track = trackvisibilityTrack(revealStats = true)
            setUpMocksForExistingTrack(track, session)

            val trackRepLike = Await.result(tracksService.track(session, trackRequest))

            trackRepLike match {
              case Some(rep) =>
                val json = Json.toJson(rep)
                json \ "playback_count" ==== JsDefined(JsNumber(111))
                json \ "download_count" ==== JsDefined(JsNumber(222))
                json \ "favoritings_count" ==== JsDefined(JsNumber(333))
                json \ "comment_count" ==== JsDefined(JsNumber(444))
                json \ "reposts_count" ==== JsDefined(JsNumber(555))
              case None =>
            }
          }

          "includes comment_count if reveal_comments = true" in new Context {
            val track = trackvisibilityTrack(revealStats = true)
            setUpMocksForExistingTrack(track, session)

            val trackRepLike = Await.result(tracksService.track(session, trackRequest))

            trackRepLike match {
              case Some(rep) =>
                val json = Json.toJson(rep)
                json \ "comment_count" ==== JsDefined(JsNumber(444))
              case None =>
            }
          }

          "does not include comment_count if reveal_comments = false" in new Context {
            val track = trackvisibilityTrack(revealStats = true, revealComments = false)
            setUpMocksForExistingTrack(track, session)

            val trackRepLike = Await.result(tracksService.track(session, trackRequest))

            trackRepLike match {
              case Some(rep) =>
                val json = Json.toJson(rep)
                json.as[JsObject].keys.contains("comment_count") ==== false
              case None =>
            }
          }

          "returns empty counts if Stitch is failing" in new Context {
            val track = trackvisibilityTrack(revealStats = true)
            setUpMocksForExistingTrack(track, session)
            when(stitchClient.countsForTracks(session, Set((trackOwnerUrn, trackUrn))))
              .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

            val trackRepLike = Await.result(tracksService.track(session, trackRequest))

            trackRepLike match {
              case Some(rep) =>
                val json = Json.toJson(rep)
                json \ "playback_count" ==== JsDefined(JsNumber(0))
                json \ "download_count" ==== JsDefined(JsNumber(0))
                json \ "favoritings_count" ==== JsDefined(JsNumber(0))
                json \ "comment_count" ==== JsDefined(JsNumber(0))
                json \ "reposts_count" ==== JsDefined(JsNumber(0))
              case None =>
            }
          }
        }
      }
    }
  }
}
