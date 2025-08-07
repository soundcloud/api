package com.soundcloud.apipublic.client.profile

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.twitter.util.Future
import proto.soundcloud.profiles.api.ChronoDirection.desc
import proto.soundcloud.profiles.api.{ChronoParams, ChronoResponse, GetTracksChronoRequest, ProfilesService}

class ProfilesClient(profilesService: ProfilesService) {

  def fetchTracksUploadedByUserFromProfiles(
      session: UserSession,
      userUrn: Urn,
      pageSize: Int,
      cursor: String
  ): Future[ChronoResponse] = {
    val request = GetTracksChronoRequest(
      Some(session.asProtoSession),
      userUrn.toString,
      Some(
        ChronoParams(
          limit = pageSize,
          direction = desc,
          cursor = cursor
        )
      )
    )
    profilesService.getTracksChrono(request)
  }
}
