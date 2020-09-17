package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrack
import play.api.libs.json.{Json, Writes}

case class CreatedTrack(trackCoordinatorTrack: TrackCoordinatorTrack) {
  def location = trackCoordinatorTrack.uri
}

object CreatedTrack {
  implicit val writes: Writes[CreatedTrack] = Writes[CreatedTrack] { createdTrack =>
    val secretToken =
      if (createdTrack.trackCoordinatorTrack.public) None else createdTrack.trackCoordinatorTrack.secret_token

    Json.obj(
      "id" -> Urn.parse(createdTrack.trackCoordinatorTrack.urn).get.identifier.toLong,
      "kind" -> "track",
      "permalink" -> createdTrack.trackCoordinatorTrack.permalink,
      "secret_token" -> secretToken,
      "urn" -> createdTrack.trackCoordinatorTrack.urn
    )
  }
}
