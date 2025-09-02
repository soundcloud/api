package com.soundcloud.apipublic.service.likes

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.periskop.client.Severity.Info

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

  def compareAndReport(
      endpoint: String,
      userUrn: String,
      likesItems: Seq[LikeItem],
      likesV2Items: Seq[LikeItem]
  ) = {
    if (likesItems == likesV2Items) {
      equalResponsesCounter.labels(endpoint).inc()
    } else {
      differentResponsesCounter.labels(endpoint).inc()
      exceptionCollector.addMessage(
        "different_likes_response",
        s"Different likes response for endpoint: $endpoint: " +
          s"user: $userUrn" +
          s"likes items: ${likesItems.mkString(", ")}, " +
          s"likes v2 items: ${likesV2Items.mkString(", ")}",
        Info
      )
    }
  }
}
