package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn

case class StreamRequest(urn: Urn, secretToken: Option[String], transcodingId: String, protocol: String)
