package com.soundcloud.apipublic.client.tracks

import com.soundcloud.jvmkit.module.util.Urn

case class DownloadRequest(urn: Urn, secretToken: Option[String], skipLogging: Boolean)
