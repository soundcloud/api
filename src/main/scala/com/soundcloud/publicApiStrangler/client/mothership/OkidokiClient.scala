package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.twitter.util.Future
import play.api.libs.json.JsObject

class OkidokiClient(service: JsonClient)
    extends MoshimoshiClient(
      service
    ) {

  def fetch(session: UserSession, urns: Set[Urn]): Future[List[JsObject]] =
    fetchByUrns(service, session, Path() / "fetch", urns)

}
