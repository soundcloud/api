package com.soundcloud.apipublic.client.followcounts

import com.soundcloud.apipublic.support.BatchingUtilities._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.util.config.{Config, DataSensitivity}
import com.twitter.util.Future
import proto.soundcloud.follows.api.GetTargetCountsBatchResponse

import scala.util.control.NonFatal

class FollowCountsClient(twirpClient: FollowsCountsTwirpClient, config: Config) {
  private val batchSize = config.get("FOLLOW_COUNTS_FETCH_MAX_ENTRIES", DataSensitivity.NON_SENSITIVE).toInt

  def counts(userUrns: Seq[Urn]): Future[Seq[FollowCounts]] = {
    val userIds = userUrns.map(_.identifier).distinct

    batch(batchSize, userIds) { userIds =>
      getFollowsCountsFollows(userIds)
    }
  }

  private def getFollowsCountsFollows(userIds: Seq[String]): Future[Seq[FollowCounts]] = {
    twirpClient
      .counts(userIds.map(Urn("soundcloud", "users", _)))
      .handle {
        case NonFatal(_) => GetTargetCountsBatchResponse(Seq.empty)
      }
      .map {
        _.counts.flatMap { c =>
          Urn.parse(c.userUrn).toOption.map { urn =>
            FollowCounts(
              userUrn = urn,
              followers = c.metric.map(_.followedBy).getOrElse(0L),
              followings = c.metric.map(_.follows).getOrElse(0L)
            )
          }
        }
      }

  }
}
