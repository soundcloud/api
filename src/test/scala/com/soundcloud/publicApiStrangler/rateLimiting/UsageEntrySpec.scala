package com.soundcloud.publicApiStrangler.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.DateTime
import com.soundcloud.jvmkit.ResourceName

class UsageEntrySpec extends UnitSpecification {
  val resource = new ResourceName("banana")
  val consumer = new Consumer {
    override def identifier: String = "234.100.12.33"
  }

  "is a factor of time window and utiliser identification" in {
    UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2001, 1, 1, 1, 1, 1, 1))).key must be_==("|BANANA|60m_1|234_100_12_33|")
    UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2010, 10, 10, 10, 10, 10, 10))).key must be_==("|BANANA|60m_10|234_100_12_33|")
  }

  "is a value object" in {
    val one = UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2001, 1, 1, 1, 1, 1, 1)))
    val same = UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2001, 1, 1, 1, 1, 1, 1)))

    one must be_==(same)
  }
}
