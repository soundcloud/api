package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.framework.FinagleBasedServer
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.finagle.http.HttpServer

object PublicApiStranglerApp extends FinagleBasedServer with PublicApiClientComponent {
  override def config = new BazookaConfig

  override def createRoutes(httpServer: HttpServer): Unit = {
    val dispatchToPublicApiHandler = new DispatchToMothershipHandler(publicApiClient)
    httpServer.registerFallback(dispatchToPublicApiHandler)
  }
}
