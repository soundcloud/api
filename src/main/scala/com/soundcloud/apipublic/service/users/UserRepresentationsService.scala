package com.soundcloud.apipublic.service.users

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.client.followcounts.FollowCountsClient
import com.soundcloud.apipublic.client.mothership.OkidokiClient
import com.soundcloud.apipublic.client.mothership.response.mapper.UserRepresentationMapper
import com.soundcloud.apipublic.client.mothership.response.representation.UserRepresentation
import com.soundcloud.apipublic.client.reposts.RepostsClient
import com.soundcloud.apipublic.service.users.UserOrderingUtils.sortByProvidedUrns
import com.soundcloud.apipublic.subscriptions.SubmarineClient
import com.soundcloud.apipublic.subscriptions.SubmarineCreatorSubscription
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.twitter.util.Future
import proto.soundcloud.likes.api.{BatchGetUserLikeCountRequest, LikesClientProtobuf}

import scala.util.control.NonFatal

class UserRepresentationsService(
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    okidokiClient: OkidokiClient,
    likesClient: LikesClientProtobuf,
    submarineClient: SubmarineClient,
    exceptionCollector: ExceptionCollector
) {
  def user(
      session: UserSession,
      urn: Urn
  ): Future[Option[UserRepresentation]] = {
    users(session, Seq(urn)).map(_.headOption)
  }

  def users(
      session: UserSession,
      urns: Seq[Urn],
      fetchSubscriptions: Boolean = true
  ): Future[List[UserRepresentation]] = {
    val uniqueUrns = urns.toSet
    for {
      (users, followCountsMap, repostsCountsMap, totalLikesCountMap, subscriptionsResponse) <- Future.join(
        okidokiClient.fetch(session, uniqueUrns),
        followCountsClient
          .counts(urns)
          .map(_.map(followCounts => (followCounts.userUrn, followCounts)).toMap),
        repostsClient.getRepostCountsByUrnWithFallback(session, uniqueUrns),
        getTotalLikesCount(uniqueUrns),
        maybeGetSubscriptions(fetchSubscriptions, session, uniqueUrns)
      )
      fullUsers = users.map(
        UserRepresentationMapper(
          _,
          Some(followCountsMap),
          Some(repostsCountsMap),
          Some(totalLikesCountMap),
          session.user,
          Some(subscriptionsResponse)
        )
      )
    } yield sortByProvidedUrns(fullUsers, urns).toList
  }

  private def maybeGetSubscriptions(
      fetchSubscriptions: Boolean,
      session: UserSession,
      uniqueUrns: Set[Urn]
  ): Future[Map[Urn, Option[SubmarineCreatorSubscription]]] = {
    if (fetchSubscriptions) submarineClient.fetchActiveCreatorSubscriptions(session, uniqueUrns)
    else Future.value(Map.empty)
  }

  def getTotalLikesCount(urns: Set[Urn]): Future[Map[Urn, Long]] =
    likesClient
      .getUserLikeCountBatch(
        BatchGetUserLikeCountRequest(urns.map(_.toString).toSeq)
      )
      .map(
        _.users
          .map(userCounts =>
            (Urn.parse(userCounts.userUrn).get, userCounts.trackLikesCount + userCounts.playlistLikesCount)
          )
          .toMap
      )
      .handleAndReport(exceptionCollector) {
        case NonFatal(_) => Map.empty
      }
}
