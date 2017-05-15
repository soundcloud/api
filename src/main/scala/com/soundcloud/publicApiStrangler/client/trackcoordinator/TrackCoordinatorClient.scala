package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.trackcoordinator.datatypes.Result
import com.soundcloud.publicApiStrangler.client.trackcoordinator.mapper._
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, Params}
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.twitter.util.Future

class TrackCoordinatorClient(service: JsonClient) {
  def deleteTrack(session: UserSession, trackUrn: Urn): Future[Result[Unit]] = {
    service.delete(session, Path("/tracks") / trackUrn,
      Params.empty,
      Params.empty,
      None
    ).map(TrackDeleteResponseMapper(_))
  }
}