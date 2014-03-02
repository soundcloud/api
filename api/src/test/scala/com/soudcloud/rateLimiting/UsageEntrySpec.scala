package com.soudcloud.rateLimiting

import com.soundcloud.scalakit.test.UnitSpecification
import org.joda.time.DateTime
import com.soundcloud.jvmkit.ResourceName

class UsageEntrySpec extends UnitSpecification {
  "is a factor of time window and utiliser identification" in {
    val resource = new ResourceName("banana")
    val consumer = new Consumer {
      override def identifier: String = "234.100.12.33"
    }
    UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2001,1,1,1,1,1,1))).key must be_==("|BANANA|15m_4|234_100_12_33|")
    UsageEntry(resource, consumer, DefaultTimeWindow(new DateTime(2010,10,10,10,10,10,10))).key must be_==("|BANANA|15m_40|234_100_12_33|")
  }
}
