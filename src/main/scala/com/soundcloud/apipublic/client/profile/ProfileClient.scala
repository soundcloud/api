package com.soundcloud.apipublic.client.profile

import com.soundcloud.jvmkit.module.twirp.proto.UserSessionOps.JvmkitSessionExt
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.Future
import proto.soundcloud.profiles.api.ChronoDirection.desc
import proto.soundcloud.profiles.api.{
  ChronoParams,
  ChronoResponse,
  GetTracksChronoRequest,
  ProfilesService,
  ResolvePermalinkRequest
}

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

  def resolvePermalink(session: UserSession, permalink: String): Future[Option[Urn]] = {
    val request = ResolvePermalinkRequest(Some(session.asProtoSession), permalink)
    profilesService
      .resolvePermalink(request)
      .map(_.urn.flatMap(urn => Urn.parse(urn).toOption))
      .rescue {
        case e: TwinagleException if e.code == ErrorCode.NotFound => Future.None
        case e: TwinagleException =>
          Future.exception(new RuntimeException(s"unexpected response from profiles: ${e.msg}"))
      }
  }
}
