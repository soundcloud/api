package com.soundcloud.apipublic.service.storefront

import com.soundcloud.apipublic.client.GatekeeperClient
import com.soundcloud.apipublic.client.fanmonetization.FanMonetizationClient
import com.soundcloud.apipublic.service.storefront.StorefrontService.ExternalPurchaseOptionsFeature
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.outcome._
import com.soundcloud.jvmkit.module.util.Urn
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.{never, verify, when}
import proto.soundcloud.fan_monetization.api.{BuyModule, BuyModuleType}
import proto.soundcloud.tracks.api.{GetVisibleTracksResponse, Metadata, TrackMetadataService, Track => ProtoTrack}

class StorefrontServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val ownerUrn = Urn("soundcloud", "users", "123")
    val trackUrn = Urn("soundcloud", "tracks", "456")
    val session = loggedInSession(ownerUrn)

    val fanMonetizationClient = mock[FanMonetizationClient]
    val gatekeeperClient = mock[GatekeeperClient]
    val trackMetadataClient = mock[TrackMetadataService]

    val service = new StorefrontService(fanMonetizationClient, gatekeeperClient, trackMetadataClient)

    val upsert = StorefrontUpsert(
      title = "Download now",
      storefrontType = StorefrontType.Digital,
      link = "https://example.com/my-track",
      linkTitle = Some("Download"),
      description = None,
      price = Some("0")
    )

    val savedStorefront = Storefront(
      trackUrn = trackUrn,
      title = upsert.title,
      storefrontType = upsert.storefrontType,
      link = upsert.link,
      linkTitle = upsert.linkTitle,
      description = None,
      imageUrl = None,
      price = upsert.price
    )

    val existingModule = BuyModule(
      title = "Old title",
      `type` = BuyModuleType.VINYL,
      link = "https://example.com/old",
      imageUrl = Some("https://images.example.com/old.jpg"),
      price = "10",
      id = "module-id-1",
      urn = Some(trackUrn.toString)
    )

    def givenEligibility(eligible: Boolean) =
      when(gatekeeperClient.isFeatureAccessible(session, ExternalPurchaseOptionsFeature))
        .thenReturn(Future.value(eligible))

    def givenTrackOwnedBy(userUrn: Urn) =
      when(trackMetadataClient.getVisibleTracks(any()))
        .thenReturn(
          Future.value(
            GetVisibleTracksResponse(
              tracks = Seq(ProtoTrack(metadata = Some(Metadata(urn = trackUrn.toString, userUrn = userUrn.toString))))
            )
          )
        )

    def givenTrackNotFound() =
      when(trackMetadataClient.getVisibleTracks(any()))
        .thenReturn(Future.value(GetVisibleTracksResponse(tracks = Seq.empty)))

    def givenExistingModules(modules: Seq[BuyModule]) =
      when(fanMonetizationClient.getBuyModules(session, trackUrn))
        .thenReturn(Future.value(modules.good))
  }

  "StorefrontService#upsertStorefront" should {

    "create a storefront when the track has none" in new Context {
      givenEligibility(true)
      givenTrackOwnedBy(ownerUrn)
      givenExistingModules(Seq.empty)
      when(fanMonetizationClient.saveBuyModule(session, trackUrn, upsert, None))
        .thenReturn(Future.value(savedStorefront.good))

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(Good(savedStorefront))
    }

    "edit the existing storefront when the track has one" in new Context {
      givenEligibility(true)
      givenTrackOwnedBy(ownerUrn)
      givenExistingModules(Seq(existingModule))
      when(fanMonetizationClient.saveBuyModule(session, trackUrn, upsert, Some(existingModule)))
        .thenReturn(Future.value(savedStorefront.good))

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(Good(savedStorefront))
    }

    "reject creators without the external purchase options feature" in new Context {
      givenEligibility(false)
      givenTrackOwnedBy(ownerUrn)

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(
        Bad(CustomError(StorefrontNotEligible))
      )
      verify(fanMonetizationClient, never()).saveBuyModule(any(), any(), any(), any())
    }

    "reject tracks not owned by the authenticated user" in new Context {
      givenEligibility(true)
      givenTrackOwnedBy(Urn("soundcloud", "users", "999"))

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(
        Bad(NotAllowed("Track is not owned by the authenticated user"))
      )
      verify(fanMonetizationClient, never()).saveBuyModule(any(), any(), any(), any())
    }

    "return not found for missing or invisible tracks" in new Context {
      givenEligibility(true)
      givenTrackNotFound()

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(
        Bad(NotFound("Track not found"))
      )
      verify(fanMonetizationClient, never()).saveBuyModule(any(), any(), any(), any())
    }

    "prefer the track outcome over eligibility when both checks fail" in new Context {
      givenEligibility(false)
      givenTrackNotFound()

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(
        Bad(NotFound("Track not found"))
      )
      verify(fanMonetizationClient, never()).saveBuyModule(any(), any(), any(), any())
    }

    "propagate downstream failures from fetching existing storefronts" in new Context {
      givenEligibility(true)
      givenTrackOwnedBy(ownerUrn)
      when(fanMonetizationClient.getBuyModules(session, trackUrn))
        .thenReturn(Future.value(Bad(CustomError(StorefrontNotEligible))))

      Await.result(service.upsertStorefront(session, trackUrn, upsert)) must beEqualTo(
        Bad(CustomError(StorefrontNotEligible))
      )
      verify(fanMonetizationClient, never()).saveBuyModule(any(), any(), any(), any())
    }
  }
}
