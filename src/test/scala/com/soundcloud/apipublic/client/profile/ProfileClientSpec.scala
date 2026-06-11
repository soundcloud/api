package com.soundcloud.apipublic.client.profile

import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.jvmkit.module.util.session.AnonymousUserSession
import com.soundcloud.jvmkit.module.util.{Geo, Urn}
import com.soundcloud.twinagle.{ErrorCode, TwinagleException}
import com.twitter.util.{Await, Future}
import org.specs2.mock.Mockito
import proto.soundcloud.common.session.{UserSession => ProtoUserSession}
import proto.soundcloud.profiles.api.ChronoDirection.desc
import proto.soundcloud.profiles.api._

class ProfilesClientSpec extends UnitSpecification with Mockito {

  trait Context extends Scope {
    val profilesService = mock[ProfilesService]
    val profilesClient = new ProfilesClient(profilesService)
    val session = AnonymousUserSession(None, Set.empty, Set.empty, Geo.UNKNOWN_GEO)
    val protoSession = ProtoUserSession.defaultInstance.copy(geo = Some(proto.soundcloud.common.session.Geo("--")))
    val limit = 2
    val cursor = "cursor"
    val userUrn = Urn("soundcloud", "users", "1")
    val trackUrn1 = Urn("soundcloud", "tracks", "123")
    val trackUrn2 = Urn("soundcloud", "tracks", "234")
  }

  "fetches tracks uploaded by user" in new Context {

    val request = GetTracksChronoRequest(
      Some(protoSession),
      userUrn.toString,
      Some(
        ChronoParams(
          limit = limit,
          direction = desc,
          cursor = cursor
        )
      )
    )
    val response = ChronoResponse(
      Seq(ChronoItem(trackUrn1.toString), ChronoItem(trackUrn2.toString))
    )
    doReturn(Future.value(response)).when(profilesService).getTracksChrono(request)
    Await.result(profilesClient.fetchTracksUploadedByUserFromProfiles(session, userUrn, limit, cursor)).items ====
      Seq(
        ChronoItem(trackUrn1.toString),
        ChronoItem(trackUrn2.toString)
      )
  }

  "resolves permalink to urn" in new Context {
    val permalink = "https://soundcloud.com/some-user/some-track"
    val response = ResolvePermalinkResponse(urn = Some(trackUrn1.toString))
    doReturn(Future.value(response)).when(profilesService).resolvePermalink(any)
    Await.result(profilesClient.resolvePermalink(session, permalink)) ==== Some(trackUrn1)
  }

  "returns None when profiles responds with NotFound" in new Context {
    val permalink = "https://soundcloud.com/some-user/some-track"
    doReturn(Future.exception(TwinagleException(ErrorCode.NotFound, "not found")))
      .when(profilesService)
      .resolvePermalink(any)
    Await.result(profilesClient.resolvePermalink(session, permalink)) ==== None
  }
}
