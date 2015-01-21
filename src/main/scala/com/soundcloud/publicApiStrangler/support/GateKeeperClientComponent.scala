package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.web.BffController
import com.soundcloud.bff.{ConfigComponent, BffComponent}
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
import com.soundcloud.service.client.GatekeeperClient



trait GateKeeperClientComponent extends BffComponent {
  this: BffController =>

  lazy val gatekeeperJsonClient =
    JsonClient(ResourceName("gatekeeper"),
      ServiceEntryPoint(config.get(ResourceName("GATEKEEPER"), ConfigConvention.SRV_RECORD)),
      config, telemetry)

  lazy val gatekeeperClient = new GatekeeperClient(gatekeeperJsonClient)

}