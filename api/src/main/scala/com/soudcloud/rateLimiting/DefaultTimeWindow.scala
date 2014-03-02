package com.soudcloud.rateLimiting

import org.joda.time.DateTime
import com.twitter.util.TimeConversions._

object DefaultTimeWindow {
  val length = 15.minutes

  def apply(currentTime: DateTime): TimeWindow = {
    val currentTimeInMinutes = currentTime.minuteOfDay.get()
    val lengthInMinutes = length.inMinutes

    val nearestWindow = (currentTimeInMinutes / lengthInMinutes)
    TimeWindow(length, nearestWindow)
  }
}
