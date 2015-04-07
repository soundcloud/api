package com.soundcloud.publicApiStrangler.clients

import com.soundcloud.bff.services.JsonService
import com.soundcloud.bff.{ConfigComponent, ServiceConfig}
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.scalakit.ResourceName

trait FollowsComponent {
  this: ConfigComponent =>

  private val followsService = JsonService(
    ServiceConfig("follows", config.get(ResourceName("FOLLOWS"), ConfigConvention.BASE_URL), config)
  )

  lazy val followsClient = new FollowsClient(followsService)

}
