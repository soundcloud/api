package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.service.client.SimilarSoundsClient

trait SimilarSoundsComponent {
  self: ScAppComponent =>

  val similarSoundsJsonClient =
    JsonClient(
      ResourceName("similar_sounds"),
      ServiceEntryPoint(config.get(ResourceName("SIMILAR_SOUNDS"), ConfigConvention.SRV_RECORD)),
      config,
      telemetry
    )

  val similarSoundsClient = new SimilarSoundsClient(similarSoundsJsonClient)

}
