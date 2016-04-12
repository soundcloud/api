package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.service.client.OkidokiClient

trait OkidokiComponent {
  self: ScAppComponent =>

  val okidokiJsonClient =
    JsonClient(
      ResourceName("okidoki"),
      ServiceEntryPoint(config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )

  val okidokiClient = new OkidokiClient(okidokiJsonClient)

}
