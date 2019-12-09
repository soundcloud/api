package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.json.UrnFormat._
import play.api.libs.json.{Json, Reads, Writes}

object DownloadRequest {
  implicit val reads: Reads[DownloadRequest] = Json.reads[DownloadRequest]
  implicit val writes: Writes[DownloadRequest] = Json.writes[DownloadRequest]
}

case class DownloadRequest(urn: Urn, secretToken: Option[String])
