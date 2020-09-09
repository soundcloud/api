package com.soundcloud.publicApiStrangler.client.mothership

import com.soundcloud.jvmkit.module.http.client.JsonClient
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper._
import com.soundcloud.publicApiStrangler.client.mothership.response.representation._
import com.soundcloud.publicApiStrangler.client.support.FetchClient
import com.twitter.util.Future

class MoshimoshiClient(
    service: JsonClient
) extends FetchClient {

  def fetchUserObjects(session: UserSession, urns: Set[Urn]): Future[List[User]] =
    fetchByUrns(service, session, Path() / "users" / "fetch", urns).map(_.map(UserMapper(_)))
}
