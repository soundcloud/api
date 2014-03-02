package com.soudcloud.publicApiStrangler

import com.soundcloud.scalakit.framework.FinagleBasedServer
import com.soundcloud.jvmkit.config.BazookaConfig
import com.soundcloud.scalakit.finagle.http.HttpServer
import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Response, Request}
import com.soudcloud.rateLimiting.framework.RateLimitComponent

object PublicApiStranglerApp extends FinagleBasedServer
                             with PublicApiClientComponent
                             with RateLimitComponent {

  override def config = new BazookaConfig

  override val customFilters: Seq[Filter[Request, Response, Request, Response]] = Seq(rateLimitingFilter)

  override def createRoutes(httpServer: HttpServer): Unit = {
    val dispatchToPublicApiHandler = new DispatchToMothershipHandler(publicApiClient)
    httpServer.registerFallback(dispatchToPublicApiHandler)
  }

}
