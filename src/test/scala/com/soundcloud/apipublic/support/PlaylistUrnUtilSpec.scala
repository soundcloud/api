package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.http.server.HandlerRequest
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.apipublic.support.PlaylistUrnUtil.getPlaylistUrn
import com.twitter.finagle.http.ParamMap
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class PlaylistUrnUtilSpec extends Specification with Mockito {
  "yields playlist urn for request containing a valid id routeparam" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("id" -> "1234")

    getPlaylistUrn(request) ==== Urn("soundcloud", "playlists", "1234")
  }

  "yields playlist urn for request containing a valid urn routeparam" in new Scope {
    val request = smartMock[HandlerRequest]
    request.routeParams returns ParamMap("id" -> "abc1234")

    getPlaylistUrn(request) ==== Urn("soundcloud", "playlists", "abc1234")
  }
}
