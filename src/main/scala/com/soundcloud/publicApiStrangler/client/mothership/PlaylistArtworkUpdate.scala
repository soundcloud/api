package com.soundcloud.publicApiStrangler.client.mothership

import play.api.libs.json.{Json, Writes}

case class PlaylistArtworkUpdate(bucket: String, filename: String)

object PlaylistArtworkUpdate {
  implicit val writes: Writes[PlaylistArtworkUpdate] = Writes { playlistArtworkUpdate =>
    Json.obj("bucket" -> playlistArtworkUpdate.bucket, "filename" -> playlistArtworkUpdate.filename)
  }
}
