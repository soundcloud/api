package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.{FollowCounts, FollowCountsClient}
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
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

    val user1 = Urn("soundcloud", "users", "123")

    val session: UserSession = loggedInSession(Urn("soundcloud", "users", "1"))

    val followCounts = Seq(FollowCounts(user1, 999, 999))
    val repostCounts = Map(user1 -> 456L)
    val requestedUrns = Seq(user1)
    val okidokiUser = Fixtures.okidokiUsersWithDeprecatedCounts
    val totalLikesCount = UserTotalLikes(user1, 2, 2)

    def stubClients() = {
      when(followCountsClient.counts(session, requestedUrns))
        .thenReturn(Future.value(followCounts))
      when(repostsClient.getRepostCountsByUrnWithFallback(session, requestedUrns.toSet))
        .thenReturn(Future.value(repostCounts))
      when(okidokiClient.fetch(session, requestedUrns.toSet))
        .thenReturn(Future.value(okidokiUser.as[List[JsObject]]))
      when(lieblingClient.userTotalLikeCount(session, requestedUrns))
        .thenReturn(Future.value(List(totalLikesCount)))
    }

    val userRepresentationService =
      new UserRepresentationsService(followCountsClient, repostsClient, okidokiClient, lieblingClient)
  }

  "#getUsers" >> {
    "Enriches users with follow counts, repost counts, public favorites count" in new Context {

      stubClients()

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.followers_count ==== Some(999)
      result.head.followings_count ==== Some(999)
      result.head.reposts_count ==== Some(456)
      result.head.public_favorites_count ==== Some(4)
    }

    "returns an empty list if okidoki returns an empty list" in new Context {
      stubClients()
      when(okidokiClient.fetch(session, requestedUrns.toSet))
        .thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.isEmpty ==== true
    }

    "follow counts are zero if follows counts client returns an empty list" in new Context {
      stubClients()
      when(followCountsClient.counts(session, requestedUrns)).thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.followers_count ==== Some(0)
      result.head.followings_count ==== Some(0)
    }

    "repost counts are zero if repost counts client returns an empty map" in new Context {
      stubClients()
      when(repostsClient.getRepostCountsByUrnWithFallback(session, requestedUrns.toSet))
        .thenReturn(Future.value(Map[Urn, Long]()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.reposts_count ==== Some(0)
    }

    "public favorites counts are zero if repost liebling client returns an empty list" in new Context {
      stubClients()
      when(lieblingClient.userTotalLikeCount(session, requestedUrns))
        .thenReturn(Future.value(List()))

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.public_favorites_count ==== Some(0)
    }

    "returns extra fields on user if it matches the logged-in user" in new Context {
      override val session = loggedInSession(user1)
      stubClients()

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.private_tracks_count ==== Some(28)
      result.head.private_playlists_count ==== Some(2)
      result.head.primary_email_confirmed ==== Some(true)
      result.head.locale ==== Some("en_GB")
    }

    "does not return extra fields on user if it does not match the logged-in user" in new Context {
      stubClients()

      val result = Await.result(userRepresentationService.getUsers(session, requestedUrns.toSet))

      result.head.private_tracks_count ==== None
      result.head.private_playlists_count ==== None
      result.head.primary_email_confirmed ==== None
      result.head.locale ==== None
    }
  }
}
