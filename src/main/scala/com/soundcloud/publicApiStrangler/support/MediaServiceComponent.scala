package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.{BffComponent, ServiceConfig}


trait MediaServiceComponent extends BffComponent {

  lazy val mediaService = configService("mediaservice", "MEDIASERVICE_BASE_URL")

  private def configService(name: String, envVar: String) = httpService(
    ServiceConfig(
      name, config.get(envVar), config
    )
  )

}
