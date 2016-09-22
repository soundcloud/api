package com.soundcloud.publicApiStrangler.client.trackcoordinator

import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent

trait TrackCoordinatorComponent {
  self: ScAppComponent =>

  val trackCoordinatorJsonClient =
    JsonClient(
      TrackCoordinatorComponent.resourceName,
      ServiceEntryPoint(config.get(TrackCoordinatorComponent.resourceName, ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )

  val trackCoordinatorClient = new TrackCoordinatorClient(trackCoordinatorJsonClient)
}

object TrackCoordinatorComponent {
  private val resourceName = ResourceName("track_coordinator")
}
