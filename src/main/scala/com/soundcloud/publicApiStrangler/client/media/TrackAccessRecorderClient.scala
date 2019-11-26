package com.soundcloud.publicApiStrangler.client.media

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params, StringParam}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class TrackAccessRecorderClient(moshimoshiClient: JsonClient) {
  def recordAccess(
      session: UserSession,
      trackUrn: Urn,
      accessFor: String,
      shouldLog: Boolean,
      maybeSecretToken: Option[String]
  ): Future[Response] = {
    val params = Seq(
      maybeSecretToken.map(secretToken => "secret_token" -> StringParam(secretToken)),
      if (shouldLog) None else Some("skip_logging" -> StringParam("1"))
    ).flatten

    moshimoshiClient.getWithSession(
      session,
      Path() / "tracks" / trackUrn / "access" / accessFor,
      Params(params: _*),
      Headers.empty
    )
  }
}
