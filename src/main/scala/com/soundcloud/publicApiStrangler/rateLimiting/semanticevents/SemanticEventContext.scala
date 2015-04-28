package com.soundcloud.publicApiStrangler.rateLimiting.semanticevents

import com.soundcloud.jvmkit.Urn
import org.joda.time.DateTime

case class SemanticEventContext(applicationName: String,
                                apiClientKey: Option[String],
                                apiClientUrn: Urn,
                                handle: String,
                                occurredAt: DateTime)
