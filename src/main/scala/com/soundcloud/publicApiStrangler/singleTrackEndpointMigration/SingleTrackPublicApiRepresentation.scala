package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import play.api.libs.json.Json

case class SingleTrackPublicApiRepresentation(
                                               kind: String,
                                               id: Long,
                                               user_id: Long
                                             )

object SingleTrackPublicApiRepresentation {
  implicit val format = Json.format[SingleTrackPublicApiRepresentation] // Sam really enjoys this
}
