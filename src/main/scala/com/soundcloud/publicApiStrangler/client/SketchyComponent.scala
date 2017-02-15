package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.publicApiStrangler.client.sketchy.SketchyClient
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent

trait SketchyComponent {
  this: ScAppComponent =>

  private val sketchyService = JsonClient(
    ResourceName("sketchy"),
    ServiceEntryPoint(config.get(ResourceName("SKETCHY"), ConfigConvention.SRV_RECORD)),
    config,
    telemetry
  )

  lazy val sketchyClient = new SketchyClient(sketchyService)
}
