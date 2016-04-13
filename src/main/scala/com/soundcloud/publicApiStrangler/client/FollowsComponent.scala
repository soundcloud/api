package com.soundcloud.publicApiStrangler.client

import com.soundcloud.follows.client.FollowsClient
import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.finagle.jsonservice.{ServiceEntryPoint, JsonClient}
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