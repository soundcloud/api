package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.bff.JsArray
import com.soundcloud.bff.finagle.{Request, ResponseBuilder}
import com.soundcloud.bff.nextbff.test.FakeUserAuthentication
import com.soundcloud.bff.security.AuthenticatorService
import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.http.HandlerRequest
import com.soundcloud.scalakit.test.VerifiedMocks
import com.twitter.finagle.http.{Request => FinagleRequest}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsNull, JsValue, Json}

class FollowCountsHelperSpec  extends UnitSpecification with Fixtures {

  trait Context extends Scope with VerifiedMocks {
    val session = mock[UserSession]
    val userAuthenticationMock = new FakeUserAuthentication(mock[AuthenticatorService])(session)
    val followCountsClientMock = mock[FollowCountsClient]
    val mothershipDispatcherMock = mock[DispatchToMothershipHandler]
    val request = new Request(mock[FinagleRequest])

    val helper = new FollowCountsHelper {
      override def userAuthentication = userAuthenticationMock
      override def useStitchForFollowCounts = () => Future.value(followCountsFlag)
      override def followCountsClient = followCountsClientMock
      override def mothershipDispatcher = mothershipDispatcherMock
    }

    val user1 = Urn("soundcloud", "users", "183")
    val user2 = Urn("soundcloud", "users", "1111")
    val user3 = Urn("soundcloud", "users", "2222")

    def followCountsFlag: Boolean

    def responseStatus: Int = 200

    def responseBody: JsValue

    def responseBuilder = new ResponseBuilder()
      .status(responseStatus)
      .body(responseBody.toString())
  }

  trait FollowCountsOffContext extends Context {
    def followCountsFlag: Boolean = false

    override def before = {
      when(mothershipDispatcherMock.dispatch(request)) thenReturn responseBuilder.toFuture
    }
  }

  trait FollowCountsOnContext extends Context {
    override def followCountsFlag = true

    def userUrns: Seq[Urn] = Seq.empty
    def followCountsSeq: Seq[FollowCounts] = Seq.empty

    override def before = {
      when(mothershipDispatcherMock.defaultHandling(any[HandlerRequest])) thenReturn Future.value(responseBuilder.build)
      when(followCountsClientMock.counts(session, userUrns)) thenReturn Future.value(followCountsSeq)
    }
  }

  "when follow counts flag is off" in new FollowCountsOffContext {
    override def responseBody = user

    Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
  }

  "when follow counts flag is on" >> {
    "with a non-JSON response" in new FollowCountsOnContext {
      override def responseBody = JsNull

      override def responseBuilder = new ResponseBuilder()
        .status(responseStatus)
        .body("No a JSON response")

      Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== "No a JSON response"
    }

    "with a non-OK status code" in new FollowCountsOnContext {
      override def responseStatus = 500

      override def responseBody = user

      Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
    }

    "with an OK status code" >> {
      "with no follow counts from client" >> {
        "with single object" in new FollowCountsOnContext {
          override def responseBody = user

          override def userUrns = Seq(user1)

          Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
        }

        "with users in the top level" in new FollowCountsOnContext {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
        }

        "with users in a collection" in new FollowCountsOnContext {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
        }
        
        "with objects containing a user" in new FollowCountsOnContext {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString() ==== responseBody.toString()
        } 
      }

      "with follow counts from client" >> {
        "with single object" in new FollowCountsOnContext {
          override def responseBody = user

          override def userUrns = Seq(user1)

          override def followCountsSeq = Seq(FollowCounts(user1, 100, 200))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          (result \ "followers_count").as[Long] ==== 100
          (result \ "followings_count").as[Long] ==== 200
        }

        "with users in the top level" in new FollowCountsOnContext {
          override def responseBody = users

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "followers_count").as[Long] ==== 100
          (values.head \ "followings_count").as[Long] ==== 200

          (values.last \ "followers_count").as[Long] ==== 300
          (values.last \ "followings_count").as[Long] ==== 400
        }

        "with users in a collection" in new FollowCountsOnContext {
          override def responseBody = usersInCollection

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          val values = (result \ "collection").as[JsArray].value

          values.size ==== 2

          (values.head \ "followers_count").as[Long] ==== 100
          (values.head \ "followings_count").as[Long] ==== 200

          (values.last \ "followers_count").as[Long] ==== 300
          (values.last \ "followings_count").as[Long] ==== 400
        }

        "with objects containing a user" in new FollowCountsOnContext {
          override def responseBody = objectsWithUsers

          override def userUrns = Seq(user2, user3)

          override def followCountsSeq = Seq(FollowCounts(user2, 100, 200), FollowCounts(user3, 300, 400))

          val result = Json.parse(Await.result(helper.dispatchToMothershipWithFollowCounts(request)).build.getContentString())
          val values = result.as[JsArray].value

          values.size ==== 2

          (values.head \ "user" \ "followers_count").as[Long] ==== 100
          (values.head \ "user" \ "followings_count").as[Long] ==== 200

          (values.last \ "user" \ "followers_count").as[Long] ==== 300
          (values.last \ "user" \ "followings_count").as[Long] ==== 400
        }
      }
    }
  }
}
