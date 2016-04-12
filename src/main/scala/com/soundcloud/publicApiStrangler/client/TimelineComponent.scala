package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.DataSensitivity
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.services.timeline.TimelineJsonClient

trait TimelineComponent {
  self: ScAppComponent =>

  val timelineJsonClient =
    JsonClient(
      ResourceName("timeline"),
      ServiceEntryPoint(config.get("TIMELINE_SRV_RECORD",DataSensitivity.NON_SENSITIVE)),
      config,
      telemetry
    )

  val timelineClient = new TimelineJsonClient(timelineJsonClient)

}