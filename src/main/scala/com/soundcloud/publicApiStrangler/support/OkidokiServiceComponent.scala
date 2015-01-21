package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.{BffComponent, ServiceConfig}


trait OkidokiServiceComponent extends BffComponent {

  lazy val okidokiService = configService("okidoki", "OKIDOKI_BASE_URL")

  private def configService(name: String, envVar: String) = httpService(
    ServiceConfig(
      name, config.get(envVar), config
    )
  )
}
