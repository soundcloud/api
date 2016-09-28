package com.soundcloud.publicApiStrangler.client.follows

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent

trait FollowsComponent {
  this: ScAppComponent =>

  private val followsService = JsonClient(
    ResourceName("follows"),
    ServiceEntryPoint(config.get(ResourceName("FOLLOWS"), ConfigConvention.SRV_RECORD)),
    config,
    telemetry
  )

  lazy val followsClient = new FollowsClient(followsService)

}
