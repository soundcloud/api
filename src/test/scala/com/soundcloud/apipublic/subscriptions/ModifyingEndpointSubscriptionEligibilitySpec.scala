package com.soundcloud.apipublic.subscriptions

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.test.UnitSpecification
import com.twitter.finagle.http.Method

class ModifyingEndpointSubscriptionEligibilitySpec extends UnitSpecification {

  "requiresProUnlimited" >> {
    "returns true for protected track mutations" >> {
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/tracks") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/tracks-after-upload") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Put, Routing.trackIdPath) must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(
        Method.Put,
        s"${Routing.trackIdPath}/storefront"
      ) must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, Routing.trackIdPath) must beTrue
    }

    "returns true for protected playlist mutations" >> {
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/playlists") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/me/playlists") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Put, Routing.playlistIdPath) must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Put, "/me/playlists/:trackId") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, Routing.playlistIdPath) must beTrue
    }

    "returns true for protected social and engagement mutations" >> {
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/me/followings/:other_id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Put, "/me/followings/:other_id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, "/me/followings/:other_id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/likes/tracks/:trackId") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, "/likes/tracks/:trackId") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/likes/playlists/:id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, "/likes/playlists/:id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/reposts/tracks/:trackId") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, "/reposts/tracks/:trackId") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/reposts/playlists/:id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Delete, "/reposts/playlists/:id") must beTrue
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(
        Method.Post,
        s"${Routing.trackIdPath}/comments"
      ) must beTrue
    }

    "returns false for read-only and out-of-scope endpoints" >> {
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Get, Routing.trackIdPath) must beFalse
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Get, "/me") must beFalse
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, Routing.grantExchangePath) must beFalse
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, Routing.muzookaWebhook) must beFalse
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/disconnect") must beFalse
      ModifyingEndpointSubscriptionEligibility.requiresProUnlimited(Method.Post, "/me/apps") must beFalse
    }

    "applies subscription checks only to the low gateway access label" >> {
      ModifyingEndpointSubscriptionEligibility.subscriptionCheckAppliesToAccessLabel(Some("low")) must beTrue
      ModifyingEndpointSubscriptionEligibility.subscriptionCheckAppliesToAccessLabel(Some("default")) must beFalse
      ModifyingEndpointSubscriptionEligibility.subscriptionCheckAppliesToAccessLabel(Some("other")) must beFalse
      ModifyingEndpointSubscriptionEligibility.subscriptionCheckAppliesToAccessLabel(None) must beFalse
    }
  }
}
