package com.soundcloud.publicApiStrangler.rateLimiting

import com.twitter.util.Duration

case class TimeWindow(unitOfMeasure: Duration, number: Int) {
  val key = s"${unitOfMeasure.inMinutes}m_$number"
}
