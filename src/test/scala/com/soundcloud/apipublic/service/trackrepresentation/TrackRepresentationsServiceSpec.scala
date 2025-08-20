package com.soundcloud.apipublic.service.trackrepresentation

import com.soundcloud.apipublic.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.apipublic.client.mothership.RichOkidokiClient
import com.soundcloud.apipublic.client.trackmetadata.TrackmetadataClient
import com.soundcloud.apipublic.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService
import com.soundcloud.apipublic.service.TrackVisibilityService.DefaultTrackFieldMask
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json._

class TrackRepresentationsServiceSpec extends TrackRepresentationsSpecificationContext {
  trait Context extends TrackRepresentationsContext {
    implicit val trackRepresentationWrites = TrackRepresentation.writes

    val trackVisibilityService = mock[TrackVisibilityService]
    val okidokiClient = mock[RichOkidokiClient]
    val likedTracksService = mock[LikedTracksService]
    val trackmetadataClient = mock[TrackmetadataClient]
    val followCountsClient = mock[FollowCountsClient]
    val trackPagination = mock[TrackPagination]

    val tracksService = new TrackRepresentationsService(
      trackVisibilityService,
      okidokiClient,
      followCountsClient,
      likedTracksService
    )

    def setUpMocksForExistingTrack(
        track: VisibleTrack,
        session: UserSession
    ) = {
      when(
        trackVisibilityService
          .visibleTracks(session, List(trackRequest), DefaultTrackFieldMask, AccessParams.explicitAccess)
      ).thenReturn(Future.value(List(track)))
      when(okidokiClient.fetchUserObjects(session, Set(requestingUserUrn)))
        .thenReturn(Future.value(List(requestingUser)))
      when(okidokiClient.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(label)))
      when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.value(List(trackOwner)))
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn))).thenReturn(Future.value(geoblockings))
      when(likedTracksService.getLikedTracks(session, Seq(trackUrn))).thenReturn(Future.value(Map(trackUrn -> true)))
      when(followCountsClient.counts(Seq(trackOwnerUrn)))
        .thenReturn(Future.value(Seq(FollowCounts(trackOwnerUrn, 0, 0))))
    }

    def setUpMocksForNonExistingTrack = {
      when(
        trackVisibilityService
          .visibleTracks(session, List(trackRequest), DefaultTrackFieldMask, AccessParams.explicitAccess)
      ).thenReturn(Future.value(List.empty))
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn))).thenReturn(Future.value(geoblockings))
    }
  }

  "#tracks" >> {

    "Returns Some(x) for public tracks" in new Context {
      val track = trackVisibilityTrack()
      setUpMocksForExistingTrack(track, session)

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))

      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentation]
        case None =>
      }
    }

    "Returns Some(x) for private tracks if the owner is requesting" in new Context {
      val track = trackVisibilityTrack(isPublic = false)
      val ownerSession = new UserSessionBuilder().setUser(trackOwnerUrn).build
      setUpMocksForExistingTrack(track, ownerSession)

      val trackRepLike = Await.result(tracksService.track(ownerSession, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentation]
        case None =>
      }
    }

    "Returns Some(x) for private tracks if there is a correct secret token" in new Context {
      val correctSecretToken = "aSecre_t"
      val track = trackVisibilityTrack(isPublic = false, secretToken = correctSecretToken)
      override val trackRequest = TrackRequest(trackUrn, Some(correctSecretToken))
      setUpMocksForExistingTrack(track, session)

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          rep must beAnInstanceOf[TrackRepresentation]
        case None =>
      }
    }

    "Returns null ISRC when its not present" in new Context {
      val track = trackVisibilityTrack(isrc = None)
      setUpMocksForExistingTrack(track, session)

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep) \ "isrc" ==== JsDefined(JsNull)
        case None =>
      }
    }

    "Returns no geoblockings if Moshimoshi is failing" in new Context {
      val track = trackVisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn)))
        .thenReturn(Future.exception(new RuntimeException("bewm! hahahaaa")))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) => Json.toJson(rep).as[JsObject].value("available_country_codes") === JsNull
        case None =>
      }
    }

    "Returns no geoblockings if Moshimoshi returns an empty list" in new Context {
      val track = trackVisibilityTrack()
      setUpMocksForExistingTrack(track, session)
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn)))
        .thenReturn(Future.value(Map(trackUrn -> List())))

      val trackRepLike = Await.result(tracksService.track(session, trackRequest))
      trackRepLike match {
        case Some(rep) =>
          Json.toJson(rep).as[JsObject].value("available_country_codes") === JsNull
        case None =>
      }
    }

    "user_favorite" >> {
      "is true when the user has liked the track, and is logged in" in new Context {
        val track = trackVisibilityTrack()
        override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep) \ "user_favorite" ==== JsDefined(JsBoolean(true))
          case None =>
        }
      }

      "is false when the user has not liked the track, and is logged in" in new Context {
        val track = trackVisibilityTrack()
        override val session = new UserSessionBuilder().setUser(requestingUserUrn).build
        setUpMocksForExistingTrack(track, session)
        when(likedTracksService.getLikedTracks(session, Seq(trackUrn))).thenReturn(Future.value(Map(trackUrn -> false)))

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep) \ "user_favorite" ==== JsDefined(JsBoolean(false))
          case None =>
        }
      }

      "is not present when the user is not logged in" in new Context {
        val track = trackVisibilityTrack()
        override val session = anonymousSession
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].value("user_favorite") === JsNull
          case None =>
        }
      }
    }

    "user_playback_count" >> {
      "is always 1 when the user is logged in" in new Context {
        val track = trackVisibilityTrack()
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
        val track = trackVisibilityTrack()
        override val session = anonymousSession
        session.isAnonymous ==== true
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].value("user_playback_count") === JsNull
          case None =>
        }
      }
    }

    "waveform_url" >> {
      "is present when visible track contains waveform urls" in new Context {
        val track = trackVisibilityTrack()
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("waveform_url") ==== true
          case None =>
        }
      }

      "is empty when visible track has no waveform urls" in new Context {
        val track = trackVisibilityTrack().copy(waveformUrls = List.empty)
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            val json = Json.toJson(rep)
            json \ "waveform_url" ==== JsDefined(JsString(""))
          case None =>
        }
      }
    }

    "private urls" >> {
      "track token in params is correct" >> {
        "appends the track secret token to urls" in new Context {
          val track = trackVisibilityTrack().copy(
            permalinkUrl = Some("http://soundcloud.com/foo/bar"),
            public = false,
            downloadable = true
          )
          override val trackRequest = TrackRequest(trackUrn, Some("secr3t-Token"))

          val trackOwnerSession = new UserSessionBuilder().setUser(trackOwner.urn).build()
          setUpMocksForExistingTrack(track, trackOwnerSession)

          val trackRepLike = Await.result(tracksService.track(trackOwnerSession, trackRequest))
          trackRepLike match {
            case Some(rep) =>
              val json = Json.toJson(rep)
              json \ "uri" ==== JsDefined(
                JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:987?secret_token=secr3t-Token")
              )
              json \ "stream_url" ==== JsDefined(
                JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:987/stream?secret_token=secr3t-Token")
              )
              json \ "download_url" ==== JsDefined(
                JsString("https://api.soundcloud.com/tracks/soundcloud:tracks:987/download?secret_token=secr3t-Token")
              )
              json \ "permalink_url" ==== JsDefined(JsString("http://soundcloud.com/foo/bar/secr3t-Token"))
            case None =>
          }
        }

        "does not add a secret token to null values" in new Context {
          val track = trackVisibilityTrack().copy(permalinkUrl = None)
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
  }
}
