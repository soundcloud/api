package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.util.Url
import org.joda.time.DateTime

/**
  * Represents a streaming URLs as returned by [[https://github.com/soundcloud/media-service media-service]]
  *
  * @param name      The name of the stream. Identifies the protocol. Comprehensive list
  *                  [[https://github.com/soundcloud/media-service/blob/master/urlgen/handlers.go#L316 here]]
  * @param url       The URL for the streaming endpoint. This URL often contains a security token and is valid only for a fixed period of time.
  * @param expiresAt The expiration date and time. Requests made after this will result in a 403 Forbidden.
  */
case class MediaUrl(name: String, url: Url, expiresAt: DateTime)
