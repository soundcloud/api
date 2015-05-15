package com.soundcloud.publicApiStrangler.support

import org.joda.time.{DateTimeZone, Period}

object TimeConversions {

  implicit class RichTwitterDuration(val d: com.twitter.util.Duration) extends AnyVal {
    def toJodaPeriod: org.joda.time.Period = {
      new Period(d.inMillis)
    }
  }

  implicit class RichTwitterTime(val t: com.twitter.util.Time) extends AnyVal {
    def toJodaDateTime: org.joda.time.DateTime = {
      new org.joda.time.DateTime(t.inMilliseconds, DateTimeZone.UTC)
    }
  }

}
