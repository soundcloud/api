package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.MeMapper
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.Me
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.twitter.util.Future
import com.soundcloud.jvmkit.module.outcome._

import scala.util.control.NonFatal

class MeService(
    userRepresentationsService: UserRepresentationsService,
    okidokiClient: OkidokiClient,
    trackCoordinatorClient: TrackCoordinatorClient,
    exceptionCollector: ExceptionCollector
) {
  def getMe(session: UserSession, meUserUrn: Urn): Future[Outcome[Me]] = {
    for {
      (userRepresentations, moshiUsers, maybeQuota) <- Future.join(
        userRepresentationsService.getUsers(session, Set(meUserUrn)),
        okidokiClient.fetch(session, Set(meUserUrn)),
        userQuota(session, Set(meUserUrn))
      )
    } yield {
      if (moshiUsers.isEmpty || userRepresentations.isEmpty)
        NotFound().bad
      else
        MeMapper(
          moshiUsers.last,
          userRepresentations.last,
          maybeQuota
        ).good
    }
  }

  private def userQuota(session: UserSession, urns: Set[Urn]): Future[Option[UserUploadQuota]] = {
    session.user match {
      case Some(urn) if urns.contains(urn) => {
        getUserQuota(session, urn).handleAndReport(exceptionCollector) {
          case NonFatal(_) => None
        }
      }
      case _ => Future.None
    }
  }

  private def getUserQuota(session: UserSession, userUrn: Urn): Future[Option[UserUploadQuota]] = {
    trackCoordinatorClient
      .uploadQuota(session, userUrn)
      .map {
        case Good(userUploadQuota) => Some(userUploadQuota)
        case _ => None
      }
  }
}
