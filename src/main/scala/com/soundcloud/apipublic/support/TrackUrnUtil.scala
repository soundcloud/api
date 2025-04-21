package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn

object TrackUrnUtil {
  def getTrackUrn(request: HandlerRequest): Urn = {
    val trackId = request.routeParams("trackId")
    Urn.parse(trackId).getOrElse(Urn("soundcloud", "tracks", trackId))
  }
}
