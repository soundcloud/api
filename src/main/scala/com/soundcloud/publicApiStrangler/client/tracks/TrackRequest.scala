package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn

case class TrackRequest(urn: Urn, secretToken: Option[String])
