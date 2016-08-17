package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.{Artwork, EmbeddingPermission, Track}
import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.test.UnitSpecification
import play.api.libs.json.Json

class TrackRepresentationSpec extends UnitSpecification {

  trait Context extends Scope {
    val trackUrn = new Urn("soundcloud", "tracks", "1324")
    val userUrn = new Urn("soundcloud", "users", "3456")
  }

  "serialises to JSON correctly" in new Context {
    val track = Track(trackUrn, Urn("soundcloud:users:112"), false, None, null, None, false, 0, None, null, null, None,
      true, null, List.empty, List.empty, null, None, None, false, false, false, None, null, None, None, None, None,
      EmbeddingPermission.None, None, Artwork(None), None)
    val trackRepresentation = TrackRepresentation(track, userUrn)

    Json.toJson(trackRepresentation) ==== Json.obj(
      "kind" -> "track",
      "id" -> 1324,
      "user_id" -> 3456
    )
  }
}
