package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn

object PlaylistUrnUtil {
  def getPlaylistUrn(request: HandlerRequest): Urn = {
    val IdParamPattern = "^(\\d+)$".r
    request.routeParams("id") match {
      case IdParamPattern(id) => Urn("soundcloud", "playlists", id)
      case other => throw new IllegalArgumentException(s"Invalid playlist id: '$other'")
    }
  }
}
