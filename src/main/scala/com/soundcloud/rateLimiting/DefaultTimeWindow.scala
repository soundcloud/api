package com.soundcloud.rateLimiting

import org.joda.time.DateTime
import com.twitter.util.TimeConversions._

object DefaultTimeWindow {
  val length = 1.hour

  def apply(currentTime: DateTime): TimeWindow = {
    val currentTimeInMinutes = currentTime.minuteOfDay.get()
    val lengthInMinutes = length.inMinutes

    val nearestWindow = currentTimeInMinutes / lengthInMinutes
    TimeWindow(length, nearestWindow)
  }
}
