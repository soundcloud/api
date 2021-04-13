package com.soundcloud.publicApiStrangler.service

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.tracks.{TrackRequest, VisibleTrackBuilder}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
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
    val clientApplication = Urn("soundcloud", "applications", "999")
    lazy val allowlistedClients = Set.empty[Urn]
    lazy val userUrn = Urn("soundcloud", "users", "123")
    lazy val session =
      (new UserSessionBuilder).setUser(userUrn).setAgent(clientApplication).build()
    val trackUrn = Urn("soundcloud", "tracks", "432")
    val tracksTwinagleClient = mock[TrackMetadataService]
    val mapper = smartMock[VisibleTrackMapper]
    lazy val service = new TrackVisibilityService(
      tracksTwinagleClient,
      mapper,
      allowlistedClients
    )
    val trackRequest = TrackRequest(trackUrn, None)
    val transcodings = List(
      Transcoding("mp3-uuid", "preset", "audio/mpeg", List("progressive"), None, "sq", 180000, None)
    )
    lazy val visibleTrack =
      (new VisibleTrackBuilder)
        .setUrn(trackUrn)
        .setUserUrn(userUrn)
        .setDisabledAt(None)
        .setTranscodings(transcodings)
        .build

    val protoTrack = ProtoTrack()
    val request = GetVisibleTracksRequest(
      trackRequests =
        List(trackRequest).map(trackRequest => ProtoTrackRequest(trackRequest.urn.toString, trackRequest.secretToken)),
      trackFieldMask = Some(TrackVisibilityService.TrackFieldMask),
      userSession = Some(session.asProtoSession)
    )

    tracksTwinagleClient.getVisibleTracks(request) returns Future.value(
      GetVisibleTracksResponse(tracks = Seq(protoTrack))
    )
    mapper.apply(protoTrack) returns visibleTrack
  }

  "#tracks" >> {
    "returns visible tracks" in new Context {
      Await.result(service.tracks(session, List(trackRequest))) ==== List(visibleTrack.good)
    }

    "track not available for api streaming" >> {
      trait NotApiStreamableTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setApiStreamable(Some(false)).build
      }

      "filters out disabled tracks" in new NotApiStreamableTrackContext {
        Await.result(service.tracks(session, List(trackRequest))) ==== List(
          CustomError(UnavailableByPolicy(trackUrn, Reason.NOT_SUPPORTED)).bad
        )
      }
    }

    "disabled track" >> {
      trait DisabledTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(Some(LocalDateTime.now())).build
      }

      "filters out disabled tracks" in new DisabledTrackContext {
        Await.result(service.tracks(session, List(trackRequest))) ==== List(
          CustomError(UnavailableByPolicy(trackUrn, Reason.UNKNOWN)).bad
        )
      }
    }

    "transcoding filter track" >> {
      trait TranscodingFilterTrackContext extends Context {
        override lazy val visibleTrack =
          (new VisibleTrackBuilder).setUrn(trackUrn).setTranscodings(List.empty).build
      }

      "filters out non 'audio/mpeg' tracks" in new TranscodingFilterTrackContext {
        Await.result(service.tracks(session, List(trackRequest))) ==== List(
          CustomError(UnavailableByPolicy(trackUrn, Reason.UNKNOWN)).bad
        )
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
        "filters out track" in new HighTierFilterTrackContext {
          Await.result(service.tracks(session, List(trackRequest))) ==== List(
            CustomError(UnavailableByPolicy(trackUrn, Reason.NOT_SUPPORTED)).bad
          )
        }
      }

      "trusted application" >> {
        trait HighTierAllowlistedAppFilterTrackContext extends HighTierFilterTrackContext {
          override lazy val allowlistedClients = Set(clientApplication)
        }

        "does not filter out track" in new HighTierAllowlistedAppFilterTrackContext {
          Await.result(service.tracks(session, List(trackRequest))) ==== List(visibleTrack.good)
        }
      }
    }
  }

  "#visibleTracks" >> {
    "returns visible tracks" in new Context {
      Await.result(service.visibleTracks(session, List(trackRequest))) ==== List(visibleTrack)
    }

    "filters out bad tracks" in new Context {
      override lazy val visibleTrack =
        (new VisibleTrackBuilder).setUrn(trackUrn).setDisabledAt(Some(LocalDateTime.now())).build
      Await.result(service.visibleTracks(session, List(trackRequest))) ==== List.empty
    }
  }
}
