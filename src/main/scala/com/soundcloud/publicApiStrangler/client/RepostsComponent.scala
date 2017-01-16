package com.soundcloud.publicApiStrangler.client

import com.soundcloud.jvmkit.ResourceName
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.scalakit.finagle.dns.ServiceEntryPoint
import com.soundcloud.scalakit.finagle.jsonservice.JsonClient
import com.soundcloud.scalakit.framework.ScAppComponent

trait RepostsComponent {
  this: ScAppComponent =>

  private val repostsService = JsonClient(
    ResourceName("reposts"),
    ServiceEntryPoint(config.get(ResourceName("REPOSTS"), ConfigConvention.SRV_RECORD)),
    config,
    telemetry
  )

  val repostsClientBuilder = (followCountsClient: FollowCountsClient, okidokiService: JsonClient) => new RepostsClient(repostsService, followCountsClient, okidokiService)

}
