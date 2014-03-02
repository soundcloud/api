package com.soudcloud.rateLimiting

import com.twitter.util.Duration

case class TimeWindow(length: Duration, identifier: Int) {
  val key = s"${length.inMinutes}m_$identifier"
}
