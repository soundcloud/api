package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.framework.FinagleBasedServer
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.finagle.http.HttpServer
import com.soudcloud.rateLimiting.framework.RateLimitComponent

object PublicApiStranglerApp extends FinagleBasedServer with PublicApiClientComponent with RateLimitComponent {

  override def config = new BazookaConfig

  override def createRoutes(httpServer: HttpServer): Unit = {
    val dispatchToPublicApiHandler = new DispatchToMothershipHandler(publicApiClient)
    val healthCheckHandler = new HealthCheckHandler

    httpServer.register("/-/health", healthCheckHandler)

    httpServer.register("/oauth2/token", enforceMaxHitsPerHour(3) andThen dispatchToPublicApiHandler)

    httpServer.registerFallback(enforceDefaultRateLimiting andThen dispatchToPublicApiHandler)
  }

}
