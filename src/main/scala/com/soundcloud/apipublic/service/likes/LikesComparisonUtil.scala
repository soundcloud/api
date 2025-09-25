package com.soundcloud.apipublic.service.likes

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.periskop.client.Severity.Info
import proto.soundcloud.likes.api.ChronoParams

class LikesComparisonUtil(telemetry: Telemetry, exceptionCollector: ExceptionCollector) {
  private val differentResponsesCounter =
    telemetry.counter(
      "different_likes_response_total",
      "Count of inconsistent likes responses between likes and likes v2 service",
      "endpoint"
    )

  private val equalResponsesCounter =
    telemetry.counter(
      "equal_likes_response_total",
      "Count of equal likes responses between likes and likes v2 service",
      "endpoint"
    )

  def compareAndReportChrono(
      endpoint: String,
      userUrn: String,
      chronoParams: Option[ChronoParams],
      likesItems: Seq[LikeItem],
      likesV2Items: Seq[LikeItem]
  ) = {
    if (likesItems == likesV2Items) {
      equalResponsesCounter.labels(endpoint).inc()
    } else {
      differentResponsesCounter.labels(endpoint).inc()

      val chronoSummary = chronoParams.map(p => s"cursor: ${p.cursor}, limit ${p.limit}").getOrElse("none")
      exceptionCollector.addMessage(
        "different_likes_response_from_user_chrono",
        s"Different likes response for endpoint: $endpoint: " +
          s" user: $userUrn," +
          s" chrono_params: $chronoSummary," +
          s" likes_items_count: ${likesItems.size}," +
          s" likes_v2_items_count: ${likesV2Items.size}," +
          s" likes_items: ${likesItems.mkString(",")}," +
          s" likes_v2_items: ${likesV2Items.mkString(",")}",
        Info
      )
    }
  }

  def compareAndReportAreLiked(
      endpoint: String,
      userUrn: String,
      likes: Seq[String],
      likesV2: Seq[String]
  ) = {
    if (likes == likesV2) {
      equalResponsesCounter.labels(endpoint).inc()
    } else {
      differentResponsesCounter.labels(endpoint).inc()
      exceptionCollector.addMessage(
        "different_likes_response_from_is_target_liked",
        s"Different likes response for endpoint: $endpoint: " +
          s"user: $userUrn" +
          s" likes_items_count: ${likes.size}," +
          s" likes_v2_items_count: ${likesV2.size}," +
          s" likes_items: ${likes.mkString(",")}," +
          s" likes_v2_items: ${likesV2.mkString(",")}",
        Info
      )
    }
  }
}
