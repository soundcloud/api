package com.soundcloud.apipublic.client.followcounts

import java.net.URLEncoder
import com.soundcloud.jvmkit.module.util.config.{Config, DataSensitivity}
import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.jvmkit.module.util.session.UserSession
import com.soundcloud.apipublic.support.BatchingUtilities._
import com.soundcloud.jvmkit.module.rollout.{Rollout, RolloutFeature}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import play.api.libs.json.{JsObject, Json}
import proto.soundcloud.follows.api.GetTargetCountsBatchResponse

import scala.util.control.NonFatal

class FollowCountsClient(
    client: JsonClient,
    twirpClient: FollowsCountsTwirpClient,
    rollout: Option[Rollout],
    config: Config
) {
  private[followcounts] val followsCountsFeature = RolloutFeature("use-follows-counts")

  private val batchSize = config.get("FOLLOW_COUNTS_FETCH_MAX_ENTRIES", DataSensitivity.NON_SENSITIVE).toInt

  def counts(session: UserSession, userUrns: Seq[Urn]): Future[Seq[FollowCounts]] = {
    val userIds = userUrns.map(_.identifier).distinct

    batch(batchSize, userIds) { userIds =>
      getFollowsCountsFeatureEnabled.flatMap { enabled =>
        if (enabled) {
          getFollowsCountsFollows(userIds)
        } else {
          getFollowsCountsStitch(session, userIds)
        }
      }

    }
  }

  private def getFollowsCountsFeatureEnabled: Future[Boolean] =
    rollout match {
      case Some(rollout) => rollout.isActive(followsCountsFeature)
      case None => Future.False
    }

  private def getFollowsCountsStitch(session: UserSession, userIds: Seq[String]): Future[Seq[FollowCounts]] = {
    val bulkParams = Params(
      "followerCounts" -> uriEncodedQueryString(params("f.b.u", userIds)),
      "followingCounts" -> uriEncodedQueryString(params("f.u", userIds))
    )
    client
      .getWithSession(session, Path() / "bulk", bulkParams, Headers.empty)
      .map { response =>
        val topLevelMap = parseBulkResponse(response)
        val followerCounts = topLevelMap("followerCounts")
        val followingCounts = topLevelMap("followingCounts")

        (followerCounts.keys ++ followingCounts.keys).toSeq.distinct.map { urn =>
          FollowCounts(urn, followerCounts.getOrElse(urn, 0), followingCounts.getOrElse(urn, 0))
        }
      }
      .handle {
        case NonFatal(e) => Seq.empty
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

  private def params(category: String, userIds: Seq[String]) = Params(
    "resolution" -> "alltime",
    "category" -> category,
    "minus-category" -> s"n.$category",
    "keys" -> userIds
  )

  private def uriEncodedQueryString(params: Params): String = {
    val flattenedParams = params.toSeq.flatMap { case (key, values) => values.value.map(key -> _) }
    val timeSeriesQuery = "/timeseries?" + flattenedParams.map(Function.tupled(_ + "=" + _)).reduceLeft(_ + "&" + _)
    URLEncoder.encode(timeSeriesQuery, "UTF-8")
  }

  private def parseBulkResponse(response: Response): Map[String, Map[Urn, Long]] = {
    response.status match {
      case Status.Ok =>
        Json.parse(response.contentString).as[JsObject].value.toMap.mapValues { individualResponseJson =>
          individualResponseJson.as[JsObject].value.toMap.flatMap {
            case (userId, jsonValue) =>
              val value = (individualResponseJson \ userId \ "series" \\ "count").headOption.map(_.as[Long])
              value.map(Urn("soundcloud", "users", userId) -> _)
          }
        }
      case _ =>
        Map.empty
    }
  }
}
