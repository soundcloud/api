package com.soudcloud.rateLimiting

import com.soundcloud.jvmkit.ResourceName


case class UsageEntry(resource:ResourceName, consumer: Consumer, time: TimeWindow) {

  def sanitise(s:String) = s.replaceAll("\\W", "_")
  val separator = UsageEntry.separator
  val name = sanitise(resource.getName)
  val timeWindowKey = sanitise(time.key)
  val consumerId = sanitise(consumer.identifier)

  val key = s"${separator}${name}${separator}${timeWindowKey}${separator}${consumerId}${separator}"
}

object UsageEntry {
  val separator = "|"
}
