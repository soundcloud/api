package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import play.api.libs.json._

case class TrackRepresentation(
  track: Track,
  user_urn: Urn
)

object TrackRepresentation {
  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue = Json.obj(
      "kind" -> "track",
      "id" -> rep.track.urn.getIdentifier.toLong,
      "user_id" -> rep.user_urn.getIdentifier.toLong
    )
  }
}
