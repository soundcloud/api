package com.soundcloud.publicApiStrangler.service.trackrepresentation

import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.WaveformUrlsGenerator
import com.soundcloud.publicApiStrangler.client.mothership.RichOkidokiClient
import com.soundcloud.publicApiStrangler.client.pubmese.PubmeseClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrack}
import com.soundcloud.publicApiStrangler.handler.support.requestParser.AccessParams
import com.soundcloud.publicApiStrangler.service.TrackVisibilityService
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json._

class TrackRepresentationsServiceSpec extends TrackRepresentationsSpecificationContext {
  trait Context extends TrackRepresentationsContext {
    implicit val trackRepresentationWrites = TrackRepresentation.writes

    val trackVisibilityService = mock[TrackVisibilityService]
    val okidokiClient = mock[RichOkidokiClient]
    val pubmeseClient = mock[PubmeseClient]
    val lieblingClient = mock[LieblingClient]
    val waveformUrlsGenerator = mock[WaveformUrlsGenerator]
    val trackmetadataClient = mock[TrackmetadataClient]
    val trackPagination = mock[TrackPagination]

    val tracksService = new TrackRepresentationsService(
      trackVisibilityService,
      okidokiClient,
      pubmeseClient,
      lieblingClient,
      waveformUrlsGenerator
    )

    def setUpMocksForExistingTrack(
        track: VisibleTrack,
        session: UserSession
    ) = {
      when(trackVisibilityService.visibleTracks(session, List(trackRequest), AccessParams.explicitAccess))
        .thenReturn(Future.value(List(track)))
      when(okidokiClient.fetchUserObjects(session, Set(requestingUserUrn)))
        .thenReturn(Future.value(List(requestingUser)))
      when(okidokiClient.fetchUserObjects(session, Set(labelUrn))).thenReturn(Future.value(List(label)))
      when(okidokiClient.fetchUserObjects(session, Set(trackOwnerUrn))).thenReturn(Future.value(List(trackOwner)))
      when(pubmeseClient.isrcsForTracks(session, Set(trackUrn))).thenReturn(Future.value(isrc()))
      when(okidokiClient.fetchTrackGeoblockings(session, Set(trackUrn))).thenReturn(Future.value(geoblockings))
      when(lieblingClient.userLikedTracks(session, Set(trackUrn), session.getUser))
        .thenReturn(Future.value(userLikedTracks))
      when(waveformUrlsGenerator.fromUid(track.uid.get)).thenReturn(waveformUrl(track.uid.get))
    }

    def setUpMocksForNonExistingTrack = {
      when(trackVisibilityService.visibleTracks(session, List(trackRequest), AccessParams.explicitAccess))
        .thenReturn(Future.value(List.empty))
      when(pubmeseClient.isrcsForTracks(session, Set(trackUrn))).thenReturn(Future.value(isrc()))
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

    "Returns null ISRC when Pubmese is failing" in new Context {
      val track = trackVisibilityTrack()
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
      "is true when the user has favourited the track, and is logged in" in new Context {
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

      "is false when the user has not favourited the track, and is logged in" in new Context {
        val track = trackVisibilityTrack()
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
      "is present when urlgen returns a stream URL" in new Context {
        val track = trackVisibilityTrack()
        setUpMocksForExistingTrack(track, session)

        val trackRepLike = Await.result(tracksService.track(session, trackRequest))
        trackRepLike match {
          case Some(rep) =>
            Json.toJson(rep).as[JsObject].keys.contains("waveform_url") ==== true
          case None =>
        }
      }
    }

    "private urls" >> {
      "track token in params is correct" >> {
        "appends the track secret token to urls" in new Context {
          val track = trackVisibilityTrack().copy(permalinkUrl = Some("http://soundcloud.com/foo/bar"), public = false)
          override val trackRequest = TrackRequest(trackUrn, Some("secr3t-Token"))

          val trackOwnerSession = new UserSessionBuilder().setUser(trackOwner.urn).build()
          setUpMocksForExistingTrack(track, trackOwnerSession)

          val trackRepLike = Await.result(tracksService.track(trackOwnerSession, trackRequest))
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
