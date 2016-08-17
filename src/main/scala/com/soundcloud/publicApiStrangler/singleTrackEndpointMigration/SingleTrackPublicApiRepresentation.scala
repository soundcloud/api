package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Urn
import play.api.libs.json._

case class SingleTrackPublicApiRepresentation(
  urn: Urn,
  user_urn: Urn
)

object SingleTrackPublicApiRepresentation {
  implicit val writes = new Writes[SingleTrackPublicApiRepresentation] {
    override def writes(rep: SingleTrackPublicApiRepresentation): JsValue = Json.obj(
      "kind" -> "track",
      "id" -> rep.urn.getIdentifier.toLong,
      "user_id" -> rep.user_urn.getIdentifier.toLong
    )
  }
}
