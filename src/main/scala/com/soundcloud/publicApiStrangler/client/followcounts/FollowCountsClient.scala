package com.soundcloud.publicApiStrangler.client.followcounts

import java.net.URLEncoder

import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.config.{Config, DataSensitivity}
import com.soundcloud.publicApiStrangler.support.BatchingUtilities._
import com.soundcloud.scalakit._
import com.soundcloud.scalakit.finagle.http.OkStatus
import com.soundcloud.scalakit.finagle.jsonservice.{JsonResponse, Params}
import com.twitter.util.{Future, NonFatal}
import play.api.libs.json.JsObject

class FollowCountsClient(client: JsonService, config: Config) {
  private val batchSize = config.get("STITCH_BULK_FETCH_MAX_ENTRIES", DataSensitivity.NON_SENSITIVE).toInt

  def counts(session: UserSession, userUrns: Seq[Urn]): Future[Seq[FollowCounts]] = {
    val userIds = userUrns.map(_.getIdentifier).distinct

    batch(batchSize, userIds) { userIds =>
      val bulkParams = Params(
        "followerCounts" -> uriEncodedQueryString(params("f.b.u", userIds)),
        "followingCounts" -> uriEncodedQueryString(params("f.u", userIds))
      )
      client.get(session, Path() / "bulk", bulkParams, Params.empty).map { response =>
        val topLevelMap = parseBulkResponse(response)
        val followerCounts = topLevelMap("followerCounts")
        val followingCounts = topLevelMap("followingCounts")

        (followerCounts.keys ++ followingCounts.keys).toSeq.distinct.map { urn =>
          FollowCounts(urn, followerCounts.getOrElse(urn, 0), followingCounts.getOrElse(urn, 0))
        }
      }.handle {
        case NonFatal(e) => Seq.empty
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

  private def parseBulkResponse(response: JsonResponse): Map[String, Map[Urn, Long]] = {
    response match {
      case JsonResponse(OkStatus, body, _, _) =>
        body.as[JsObject].value.toMap.mapValues { individualResponseJson =>
          individualResponseJson.as[JsObject].value.toMap.flatMap { case (userId, jsonValue) =>
            val value = (individualResponseJson \ userId \ "series" \\ "count").headOption.map(_.as[Long])
            value.map(Urn("soundcloud", "users", userId) -> _)
          }
        }
      case _ =>
        Map.empty
    }
  }
}
