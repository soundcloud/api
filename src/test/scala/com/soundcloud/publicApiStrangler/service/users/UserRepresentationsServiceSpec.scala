package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.{CreatorSubscription, Product}
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.subscriptions.{
  ActiveSubmarineCreatorSubscriptionsResponse,
  NoSubmarineCreatorSubscriptionsResponse,
  SubmarineClient,
  SubmarineCreatorSubscriptionsMapper
}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.JsObject

class UserRepresentationsServiceSpec extends UnitSpecification {

  trait Context extends Scope {
    val followCountsClient = mock[FollowCountsClient]
    val repostsClient = mock[RepostsClient]
    val okidokiClient = mock[OkidokiClient]
    val lieblingClient = mock[LieblingClient]
    val submarineClient = mock[SubmarineClient]

    val user1 = Urn("soundcloud", "users", "123")

    val session: UserSession = loggedInSession(Urn("soundcloud", "users", "1"))

    val followCounts = Seq(FollowCounts(user1, 999, 999))
    val repostCounts = Map(user1 -> 456L)
    val requestedUrns = Seq(user1)
    val okidokiUser = Fixtures.okidokiUsersWithDeprecatedCounts
    val totalLikesCount = UserTotalLikes(user1, 2, 2)
    val submarineSubscription = new SubmarineCreatorSubscriptionsMapper()(Fixtures.submarineCreatorSubscription)
    val uploadQuota = UserUploadQuota(1, Some(2))

    def stubClients() = {
      when(followCountsClient.counts(session, requestedUrns))
        .thenReturn(Future.value(followCounts))
      when(repostsClient.getRepostCountsByUrnWithFallback(session, requestedUrns.toSet))
        .thenReturn(Future.value(repostCounts))
      when(okidokiClient.fetch(session, requestedUrns.toSet))
        .thenReturn(Future.value(okidokiUser.as[List[JsObject]]))
      when(lieblingClient.userTotalLikeCount(session, requestedUrns))
        .thenReturn(Future.value(List(totalLikesCount)))
      submarineClient.fetchActiveCreatorSubscriptions(session, requestedUrns.toSet) returns Future.value(
        ActiveSubmarineCreatorSubscriptionsResponse(submarineSubscription)
      )
    }

    val userRepresentationService =
      new UserRepresentationsService(
        followCountsClient,
        repostsClient,
        okidokiClient,
        lieblingClient,
        submarineClient
      )
  }

  "#getUsers" >> {
    "Enriches users with follow counts, repost counts, public favorites count, creator subscriptions" in new Context {
      stubClients()

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.followers_count ==== Some(999)
      result.head.followings_count ==== Some(999)
      result.head.reposts_count ==== Some(456)
      result.head.public_favorites_count ==== Some(4)
      result.head.subscriptions ==== Seq(CreatorSubscription(Product("creator-pro", "Pro")))
    }

    "returns an empty list if okidoki returns an empty list" in new Context {
      stubClients()
      when(okidokiClient.fetch(session, requestedUrns.toSet))
        .thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.isEmpty ==== true
    }

    "returns moshi subscriptions if submarine returns no subscriptions" in new Context {
      stubClients()

      submarineClient.fetchActiveCreatorSubscriptions(session, requestedUrns.toSet) returns Future.value(
        NoSubmarineCreatorSubscriptionsResponse
      )

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.subscriptions ==== Seq.empty
    }

    "returns moshi follow counts if follows counts client returns an empty list" in new Context {
      stubClients()
      when(followCountsClient.counts(session, requestedUrns)).thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.followers_count ==== Some(0)
      result.head.followings_count ==== Some(0)
    }

    "returns moshi repost counts if repost counts client returns an empty map" in new Context {
      stubClients()
      when(repostsClient.getRepostCountsByUrnWithFallback(session, requestedUrns.toSet))
        .thenReturn(Future.value(Map[Urn, Long]()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.reposts_count ==== Some(0)
    }

    "returns moshi favorites counts are zero if repost liebling client returns an empty list" in new Context {
      stubClients()
      when(lieblingClient.userTotalLikeCount(session, requestedUrns))
        .thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.public_favorites_count ==== Some(0)
    }
  }
}
