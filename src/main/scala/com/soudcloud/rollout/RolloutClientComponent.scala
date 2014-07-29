package com.soundcloud.rollout

import com.soundcloud.scalakit.framework.ScAppComponent
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.scalakit.BaseUrl
import com.soundcloud.bff.BffController
import com.soundcloud.bff.ServiceConfig

trait RolloutClientComponent {
  self: BffController =>

  lazy val rolloutClient = {
    val config = ServiceConfig(
      ResourceName("rollout"),
      BaseUrl(cfg.get("ROLLOUT_BASE_URL")),
      cfg)

    httpService(config)
  }

}
