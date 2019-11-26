package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn

case class User(
    urn: Urn,
    permalink: String,
    username: String,
    avatar_url: String,
    permalink_url: String,
    city: Option[String],
    country: Option[String],
    tracks_count: Int,
    followers_count: Option[Int],
    followings_count: Option[Int],
    verified: Boolean,
    description: Option[String],
    updated_at: Option[String]
)
