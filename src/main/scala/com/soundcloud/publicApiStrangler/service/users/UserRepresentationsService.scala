package com.soundcloud.publicApiStrangler.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.liebling.{LieblingClient, UserTotalLikes}
import com.soundcloud.publicApiStrangler.client.mothership.OkidokiClient
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.subscriptions.SubmarineClient
import com.twitter.util.Future

class UserRepresentationsService(
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    okidokiClient: OkidokiClient,
    lieblingClient: LieblingClient,
    submarineClient: SubmarineClient
) {
  def getUsers(
      session: UserSession,
      urns: Set[Urn]
  ): Future[List[UserRepresentation]] = {
    for {
      (users, followCountsMap, repostsCountsMap, totalLikesCountMap, subscriptionsResponse) <- Future.join(
        okidokiClient.fetch(session, urns),
        followCountsClient
          .counts(session, urns.toSeq)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap),
        repostsClient.getRepostCountsByUrnWithFallback(session, urns),
        getTotalLikesCount(session, urns),
        submarineClient.fetchActiveCreatorSubscriptions(session, urns)
      )
    } yield users.map(
      UserRepresentationMapper(
        _,
        Some(followCountsMap),
        Some(repostsCountsMap),
        Some(totalLikesCountMap),
        session.user,
        Some(subscriptionsResponse.subscriptions)
      )
    )
  }

  private def getTotalLikesCount(session: UserSession, urns: Set[Urn]): Future[Map[Urn, UserTotalLikes]] =
    lieblingClient
      .userTotalLikeCount(session, urns.toSeq)
      .map(_.map(count => (count.user_urn, count)).toMap)
}
