package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn

object TrackUrnUtil {
  def trackUrn(request: HandlerRequest): Urn = {
    val IdParamPattern = "^(\\d+)$".r
    request.routeParams("trackId") match {
      case IdParamPattern(id) => Urn("soundcloud", "tracks", id)
      case other => throw new IllegalArgumentException(s"Invalid track id: '$other'")
    }
  }
}
