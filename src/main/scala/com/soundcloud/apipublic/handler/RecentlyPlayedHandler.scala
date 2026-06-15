package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.handler.support.requestParser.{AccessParams, AccessParamsExtractor}
import com.soundcloud.apipublic.service.RecentlyPlayedService
import com.soundcloud.apipublic.service.representation.collection.{Collection => TrackCollection}
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.server.{HandlerRequest, JsonResponseBuilder}
import com.twitter.finagle.http.Response
import com.twitter.util.Future

class RecentlyPlayedHandler(
    userAuthentication: UserAuthentication,
    recentlyPlayedService: RecentlyPlayedService
) {

  def getRecentlyPlayedTracks(request: HandlerRequest): Future[Response] = {
    userAuthentication.withLoggedInUser(request) { (session, user) =>
      val access = AccessParamsExtractor.unapply(request.params, predefinedAccess = AccessParams.explicitAccess)

      recentlyPlayedService
        .recentlyPlayedTracks(session, user, access)
        .map { tracks =>
          JsonResponseBuilder.ok(
            TrackCollection.getNonNullRepresentation(TrackCollection(tracks, None), hasLinkedPartitioning = true)
          )
        }
    }
  }
}
