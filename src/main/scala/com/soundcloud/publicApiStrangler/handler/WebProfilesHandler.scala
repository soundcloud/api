package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.MoshimoshiClient
import com.twitter.finagle.http.Response
import com.twitter.util.Future
import play.api.libs.json.Json

class WebProfilesHandler(
    userAuthenticator: UserAuthentication,
    moshimoshiClient: MoshimoshiClient
) {

  def getWebProfiles(request: HandlerRequest): Future[Response] = {
    userAuthenticator.withUserSession(request) { session =>
      moshimoshiClient
        .userWebProfiles(session, Urn("soundcloud", "users", request.routeParams("userId")))
        .map { webProfiles =>
          JsonResponseBuilder.ok(Json.stringify(Json.toJson(webProfiles)))
        }
    }
  }
}
