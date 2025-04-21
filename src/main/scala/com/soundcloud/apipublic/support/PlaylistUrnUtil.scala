package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn

object PlaylistUrnUtil {
  def getPlaylistUrn(request: HandlerRequest): Urn = {
    getPlaylistUrn(request.routeParams("id"))
  }

  def getPlaylistUrn(id: String): Urn = {
    Urn.parse(id).getOrElse(Urn("soundcloud", "playlists", id))
  }
}
