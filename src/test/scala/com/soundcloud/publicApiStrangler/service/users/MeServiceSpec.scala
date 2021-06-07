package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.outcome.{GoodOps, NotFound, _}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, JsObject}

class MeServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val userRepresentationsSerice = mock[UserRepresentationsService]
    val okidokiClient = mock[OkidokiClient]
    val trackCoordinatorClient = mock[TrackCoordinatorClient]
    val exceptionCollector = mock[ExceptionCollector]

    val meUrn = Urn("soundcloud", "users", "123")

    val session = loggedInSession(meUrn)

    val requestedUrns = Seq(meUrn)
    val okidokiUser: JsArray = Fixtures.okidokiUsersWithDeprecatedCounts.as[JsArray]
    val userRepresentation = UserRepresentationMapper(okidokiUser.value.last)
    val uploadQuota = UserUploadQuota(1, Some(2))

    def stubClients() = {
      okidokiClient.fetch(session, requestedUrns.toSet) returns Future.value(okidokiUser.as[List[JsObject]])
      userRepresentationsSerice.getUsers(session, Set(meUrn)) returns Future.value(List(userRepresentation))
      trackCoordinatorClient.uploadQuota(session, meUrn) returns Future.value(uploadQuota.good)
    }

    val meService =
      new MeService(
        userRepresentationsSerice,
        okidokiClient,
        trackCoordinatorClient,
        exceptionCollector
      )
  }

  "#getMe" >> {

    "enriches user with extra fields" in new Context {
      stubClients()

      val result = Await.result(meService.getMe(session, meUrn))

      result match {
        case Good(me) => {
          me.private_tracks_count ==== Some(28)
          me.private_playlists_count ==== Some(2)
          me.primary_email_confirmed ==== Some(true)
          me.locale ==== Some("en_GB")
          me.quota ==== Some(uploadQuota)
        }
        case _ => ko("should be good")
      }
    }

    "returns None for quota if fetching of quota fails with exception" in new Context {
      stubClients()

      trackCoordinatorClient.uploadQuota(session, meUrn) returns Future.exception(new Exception)

      val result = Await.result(meService.getMe(session, meUrn))

      result match {
        case Good(me) => me.quota ==== None
        case _ => ko("should be good")
      }
    }

    "returns None for quota if fetching of quota fails with a bad result" in new Context {
      stubClients()

      trackCoordinatorClient.uploadQuota(session, meUrn) returns Future.value(NotFound().bad)

      val result = Await.result(meService.getMe(session, meUrn))

      result match {
        case Good(me) => me.quota ==== None
        case _ => ko("should be good")
      }
    }

    "returns NotFound if okidoki fetch fails" in new Context {
      stubClients()

      okidokiClient.fetch(session, requestedUrns.toSet) returns Future.value(List.empty)

      val result = Await.result(meService.getMe(session, meUrn))

      result match {
        case Good(_) => ko("should be NotFound")
        case Bad(value) => value ==== NotFound()
      }
    }

    "returns NotFound if UserRepresentationsService fetch fails" in new Context {
      stubClients()

      userRepresentationsSerice.getUsers(session, Set(meUrn)) returns Future.value(List.empty)

      val result = Await.result(meService.getMe(session, meUrn))

      result match {
        case Good(_) => ko("should be NotFound")
        case Bad(value) => value ==== NotFound()
      }
    }
  }
}
