package com.soundcloud.publicApiStrangler.service.oauth

case class AccessTokenResponse(
    accessToken: String,
    expiresIn: Option[Int],
    refreshToken: Option[String],
    scope: Seq[String]
)
