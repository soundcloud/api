package com.soundcloud.apipublic.subscriptions

import com.soundcloud.apipublic.Routing
import com.twitter.finagle.http.Method

/**
  * Determines whether a modifying request requires an active Pro Unlimited subscription.
  *
  * Protected scope: track, playlist, and social/engagement mutations. OAuth/system endpoints
  * (token exchange, webhooks, disconnect, app registration) are out of scope and never match.
  */
object ModifyingEndpointSubscriptionEligibility {
  private type RoutePattern = (Method, String)

  /** Explicit opt-outs from the Pro Unlimited check within an otherwise protected route. */
  private val ExemptPatterns: Set[RoutePattern] = Set.empty

  private val ProtectedPatterns: Set[RoutePattern] = Set(
    // Tracks
    (Method.Post, "/tracks"),
    (Method.Post, "/tracks-after-upload"),
    (Method.Put, Routing.trackIdPath),
    (Method.Put, s"${Routing.trackIdPath}/storefront"),
    (Method.Delete, Routing.trackIdPath),
    // Playlists
    (Method.Post, "/playlists"),
    (Method.Post, "/me/playlists"),
    (Method.Put, Routing.playlistIdPath),
    (Method.Put, "/me/playlists/:trackId"),
    (Method.Delete, Routing.playlistIdPath),
    // Social / engagement
    (Method.Post, "/me/followings/:other_id"),
    (Method.Put, "/me/followings/:other_id"),
    (Method.Delete, "/me/followings/:other_id"),
    (Method.Post, "/likes/tracks/:trackId"),
    (Method.Delete, "/likes/tracks/:trackId"),
    (Method.Post, "/likes/playlists/:id"),
    (Method.Delete, "/likes/playlists/:id"),
    (Method.Post, "/reposts/tracks/:trackId"),
    (Method.Delete, "/reposts/tracks/:trackId"),
    (Method.Post, "/reposts/playlists/:id"),
    (Method.Delete, "/reposts/playlists/:id"),
    (Method.Post, s"${Routing.trackIdPath}/comments")
  )

  def requiresProUnlimited(method: Method, rawPattern: String): Boolean =
    ProtectedPatterns.contains((method, rawPattern)) && !ExemptPatterns.contains((method, rawPattern))

  /** Tyk policy group from gateway-admin that requires a Pro Unlimited subscription. */
  val LowAccessLabel = "low"

  def subscriptionCheckAppliesToAccessLabel(accessLabel: Option[String]): Boolean =
    accessLabel.contains(LowAccessLabel)
}
