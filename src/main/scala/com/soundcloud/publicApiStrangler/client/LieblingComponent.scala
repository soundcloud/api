package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.service.client.LieblingClient

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