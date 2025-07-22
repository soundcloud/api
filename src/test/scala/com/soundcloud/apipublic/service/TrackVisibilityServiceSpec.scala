package com.soundcloud.apipublic.service

import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.apipublic.authorization.AllowlistedClients
import com.soundcloud.apipublic.authorization.policies._
import com.soundcloud.apipublic.client.tracks.{TrackRequest, Transcoding, VisibleTrackBuilder}
import com.soundcloud.apipublic.handler.support.requestParser.AccessParams
import com.soundcloud.apipublic.service.TrackVisibilityService.DefaultTrackFieldMask
import com.soundcloud.apipublic.service.tracks.VisibleTrackMapper
import com.twitter.util.{Await, Future}
import org.joda.time.LocalDateTime
import org.specs2.matcher.Scope
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import proto.soundcloud.tracks.api.{
  GetVisibleTracksRequest,
  GetVisibleTracksResponse,
  TrackMetadataService,
  Track => ProtoTrack,
  TrackRequest => ProtoTrackRequest
}

class TrackVisibilityServiceSpec extends Specification with Mockito {

  trait Context extends Scope {
    lazy val userUrn = Urn("soundcloud", "users", "123")
    lazy val session =
      (new UserSessionBuilder).setUser(userUrn).setAgent(clientApplication).build()
    lazy val service = new TrackVisibilityService(
      tracksTwinagleClient,
      mapper
    )
    lazy val visibleTrack =
      (new VisibleTrackBuilder)
        .setUrn(trackUrn)
        .setUserUrn(userUrn)
        .setDisabledAt(None)
        .setTranscodings(transcodings)
        .setAccess(Some(Access.Playable))
        .build
    lazy val access = AccessParams()
    lazy val clientApplication = Urn("soundcloud", "applications", "999")
    val trackUrn = Urn("soundcloud", "tracks", "432")
    val tracksTwinagleClient = mock[TrackMetadataService]
    val mapper = smartMock[VisibleTrackMapper]
    val trackRequest = TrackRequest(trackUrn, None)
    val transcodings = List(
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    )
    val protoTrack = ProtoTrack()
    val fieldMask = DefaultTrackFieldMask
    val request = GetVisibleTracksRequest(
      trackRequests =
        List(trackRequest).map(trackRequest => ProtoTrackRequest(trackRequest.urn.toString, trackRequest.secretToken)),
      trackFieldMask = Some(fieldMask),
      userSession = Some(session.asProtoSession)
    )

    tracksTwinagleClient.getVisibleTracks(request) returns Future.value(
      GetVisibleTracksResponse(tracks = Seq(protoTrack))
    )
    mapper.apply(protoTrack, session) returns visibleTrack
  }

  "#tracks" >> {
    "returns visible tracks by default" in new Context {
      Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(visibleTrack.good)
    }

    "track not available for api streaming, default access" >> {
      trait NotApiStreamableTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setApiStreamable(Some(false)).build
      }

      "filters out non streamable tracks" in new NotApiStreamableTrackContext {
        Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(
          CustomError(UnavailableByPolicy(trackUrn, Reason.NOT_SUPPORTED)).bad
        )
      }
    }

    "track not available for api streaming, full access" >> {
      trait NotApiStreamableTrackContext extends Context {
        override lazy val visibleTrack = (new VisibleTrackBuilder).setUrn(trackUrn).setApiStreamable(Some(false)).build
        override lazy val access = AccessParams(Set(Access.Playable, Access.Preview, Access.Blocked))

        val expectedTrack = visibleTrack.copy(access = Some(Access.Blocked))
      }

      "returns metadata for non streamable tracks" in new NotApiStreamableTrackContext {
        Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(expectedTrack.good)
      }
    }

    "disabled track" >> {
      trait DisabledTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(Some(LocalDateTime.now())).build
      }

      "filters out disabled tracks" in new DisabledTrackContext {
        Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List.empty
      }
    }

    "paywalled track filter" >> {
      trait HighTierFilterTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder)
            .setUrn(trackUrn)
            .setDisabledAt(None)
            .setTranscodings(transcodings)
            .setAuthorization(
              new ContentAuthorization(
                trackUrn,
                ContentPolicy.MONETIZE,
                Reason.DEFAULT,
                Set.empty[ContentRestriction],
                MonetizationModel.SUB_HIGH_TIER
              )
            )
            .build
      }

      "untrusted application" >> {

        "filters out track, default access" in new HighTierFilterTrackContext {
          Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(
            CustomError(UnavailableByPolicy(trackUrn, Reason.DEFAULT)).bad
          )
        }

        "return track, full access" in new HighTierFilterTrackContext {
          override lazy val access = AccessParams(Set(Access.Playable, Access.Preview, Access.Blocked))
          val expectedTrack = visibleTrack.copy(access = Some(Access.Blocked))

          Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(expectedTrack.good)
        }
      }

      "trusted application" >> {
        trait HighTierAllowlistedAppFilterTrackContext extends Context {
          override lazy val visibleTrack =
            (new VisibleTrackBuilder)
              .setUrn(trackUrn)
              .setDisabledAt(None)
              .setTranscodings(transcodings)
              .setAuthorization(
                new ContentAuthorization(
                  trackUrn,
                  ContentPolicy.MONETIZE,
                  Reason.DEFAULT,
                  Set.empty[ContentRestriction],
                  MonetizationModel.SUB_HIGH_TIER
                )
              )
              .build
          override lazy val clientApplication = AllowlistedClients.clients.head
          val expectedTrack = visibleTrack.copy(access = Some(Access.Preview))
        }

        "does not filter out track" in new HighTierAllowlistedAppFilterTrackContext {
          Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(expectedTrack.good)
        }
      }
    }

    "blocked track filter" >> {

      trait BlockedTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder)
            .setUrn(trackUrn)
            .setDisabledAt(None)
            .setTranscodings(transcodings)
            .setAuthorization(
              new ContentAuthorization(
                trackUrn,
                ContentPolicy.BLOCK,
                Reason.GEO,
                Set.empty[ContentRestriction],
                MonetizationModel.NOT_APPLICABLE
              )
            )
            .build
      }

      "filters out track, default access" in new BlockedTrackContext {
        Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(
          CustomError(UnavailableByPolicy(trackUrn, Reason.GEO)).bad
        )
      }

      "returns track, full access" in new BlockedTrackContext {
        override lazy val access = AccessParams(Set(Access.Playable, Access.Preview, Access.Blocked))
        val expectedTrack = visibleTrack.copy(access = Some(Access.Blocked))

        Await.result(service.tracks(session, List(trackRequest), fieldMask, access)) ==== List(expectedTrack.good)
      }
    }
  }

  "#visibleTracks" >> {
    "returns visible tracks" in new Context {
      Await.result(service.visibleTracks(session, List(trackRequest), fieldMask, access)) ==== List(visibleTrack)
    }

    "filters out bad tracks" in new Context {
      override lazy val visibleTrack =
        (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(Some(LocalDateTime.now())).build
      Await.result(service.visibleTracks(session, List(trackRequest), fieldMask, access)) ==== List.empty
    }
  }
}
