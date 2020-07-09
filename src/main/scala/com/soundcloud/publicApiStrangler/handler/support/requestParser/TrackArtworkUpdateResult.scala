package com.soundcloud.publicApiStrangler.handler.support.requestParser

import play.api.libs.json.{Json, Writes}

case class TrackArtworkUpdateResult(bucket: String, filename: String)

object TrackArtworkUpdateResult {
  implicit val writes: Writes[TrackArtworkUpdateResult] = Writes { trackArtworkUpdate =>
    Json.obj("bucket" -> trackArtworkUpdate.bucket, "filename" -> trackArtworkUpdate.filename)
  }
}
