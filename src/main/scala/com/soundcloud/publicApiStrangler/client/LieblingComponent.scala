package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent

trait LieblingComponent {
  self: ScAppComponent =>

  val lieblingJsonClient =
    JsonClient(
      ResourceName("liebling"),
      ServiceEntryPoint(config.get(ResourceName("LIEBLING"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )

  val lieblingClient = new LieblingClient(lieblingJsonClient)
}
