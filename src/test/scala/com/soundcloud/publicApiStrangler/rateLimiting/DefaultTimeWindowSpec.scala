package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.DateTime

class DefaultTimeWindowSpec extends UnitSpecification {

  "day has 24 60 minutes windows in total" in {
    val everySecondInADay = for (
      hour <- (0 to 23);
      minute <- (0 to 59);
      second <- (0 to 59)
    ) yield new DateTime(2014, 2, 24, hour, minute, second)

    val all60minWindows = (0 to 23).map(TimeWindow(DefaultTimeWindow.length, _)).toSet

    val everyTimeWindowInADay = everySecondInADay.map(DefaultTimeWindow(_)).toSet
    everyTimeWindowInADay must be_==(all60minWindows)
  }

  "every 60 minutes, from the first minute of the day we have a new window" in {
    def dateTime(hour: Int, minute: Int) = new DateTime(2014, 2, 24, hour, minute, 33)

    DefaultTimeWindow(dateTime(0, 0)) must be_==(TimeWindow(DefaultTimeWindow.length, 0))
    DefaultTimeWindow(dateTime(0, 14)) must be_==(TimeWindow(DefaultTimeWindow.length, 0))

    DefaultTimeWindow(dateTime(1, 15)) must be_==(TimeWindow(DefaultTimeWindow.length, 1))

    DefaultTimeWindow(dateTime(2, 1)) must be_==(TimeWindow(DefaultTimeWindow.length, 2))
    DefaultTimeWindow(dateTime(2, 10)) must be_==(TimeWindow(DefaultTimeWindow.length, 2))

    DefaultTimeWindow(dateTime(23, 10)) must be_==(TimeWindow(DefaultTimeWindow.length, 23))
    DefaultTimeWindow(dateTime(23, 59)) must be_==(TimeWindow(DefaultTimeWindow.length, 23))
  }
}
