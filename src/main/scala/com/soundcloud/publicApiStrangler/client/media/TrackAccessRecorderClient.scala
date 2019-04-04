package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class TrackAccessRecorderClient(moshimoshiClient: JsonClient) {

  def recordStreamAccess(session: UserSession, trackUrn: Urn, shouldLog: Boolean): Future[Response] =
    recordAccess(session, trackUrn, "stream", shouldLog)

  private def recordAccess(session: UserSession, trackUrn: Urn, accessFor: String, shouldLog: Boolean): Future[Response] = {
    val params = if (shouldLog) Params.empty else Params("skip_logging" -> "1")
    moshimoshiClient.getWithSession(session, Path() / "tracks" / trackUrn / "access" / accessFor, params, Headers.empty)
  }
}
