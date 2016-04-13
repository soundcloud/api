package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.service.client.GatekeeperClient

trait GatekeeperComponent {
  self: ScAppComponent =>

  val gatekeeperJsonClient =
    JsonClient(ResourceName("gatekeeper"),
      ServiceEntryPoint(config.get(ResourceName("GATEKEEPER"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry)

  val gatekeeperClient = new GatekeeperClient(gatekeeperJsonClient)
}
