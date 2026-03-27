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
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionHandler.FutureExtensions
import com.twitter.util.Future
import proto.soundcloud.counts.api.{AllCountsRequest, CountsApiService, GetAllCountsRequest, Metrics}
import proto.soundcloud.likes.api.{BatchGetUserLikeCountRequest, LikesClientProtobuf}
import scalapb.FieldMaskUtil

import scala.util.control.NonFatal

class UserRepresentationsService(
    followCountsClient: FollowCountsClient,
    repostsClient: RepostsClient,
    okidokiClient: OkidokiClient,
    likesClient: LikesClientProtobuf,
    countsApiService: CountsApiService,
    submarineClient: SubmarineClient,
    exceptionCollector: ExceptionCollector,
    rollout: Rollout
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
          session.agent,
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

  private def useUnifiedLikeCounts = RolloutFeature("use-unified-counts")
  private val USER_LIKE_MASK = Some(
    FieldMaskUtil.selectFieldNumbers[Metrics](
      Set(
        Metrics.LIKES_FIELD_NUMBER,
        Metrics.LIKES_PLAYLISTS_FIELD_NUMBER
      )
    )
  )

  def getTotalLikesCount(urns: Set[Urn]): Future[Map[Urn, Long]] =
    rollout.isActive(useUnifiedLikeCounts).flatMap {
      case true =>
        countsApiService
          .getAllTotalCounts(
            GetAllCountsRequest.of(
              urns
                .map(urn => AllCountsRequest.of(urn.toString, USER_LIKE_MASK))
                .toSeq
            )
          )
          .map(_.counts.map(c => Urn.parse(c.urn).get -> (c.getMetrics.likes + c.getMetrics.likesPlaylists)).toMap)
          .handleAndReport(exceptionCollector) {
            case NonFatal(_) => Map.empty
          }

      case false =>
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
}
