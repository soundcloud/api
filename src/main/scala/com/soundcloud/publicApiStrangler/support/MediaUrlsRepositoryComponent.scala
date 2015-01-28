package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.{BffComponent, ServiceConfig}
import com.soundcloud.bff.media.MediaUrlsRepository


trait MediaUrlsRepositoryComponent extends BffComponent {

  lazy val mediaService = configService("mediaservice", "MEDIASERVICE_BASE_URL")
  lazy val okidokiService = configService("okidoki", "OKIDOKI_BASE_URL")

  lazy val mediaUrlsRepository = new MediaUrlsRepository(okidokiService, mediaService)

  private def configService(name: String, envVar: String) = httpService(
    ServiceConfig(
      name, config.get(envVar), config
    )
  )

}
