package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.admin.AdminServer
import com.soundcloud.jvmkit.module.bff.BffHttpServer
import com.soundcloud.jvmkit.module.bff.filters.CorsFilter
import com.soundcloud.jvmkit.module.bff.ratelimiting.facade._
import com.soundcloud.jvmkit.module.http.server.akira.ResponseDumpSessionRegistry
import com.soundcloud.jvmkit.module.http.server.config.HttpServerConfig
import com.soundcloud.jvmkit.module.http.server.HandlerRouterBuilder
import com.soundcloud.jvmkit.module.memcached.RichMemcachedClient
import com.soundcloud.jvmkit.module.memcached.config.MemcachedClientConfig
import com.soundcloud.jvmkit.module.rollout.BasicRolloutFeature
import com.soundcloud.jvmkit.module.telemetry.exceptions.{AirbrakeClient, AirbrakeConfig, ExceptionCollector}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.config.AppConfig
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.jvmkit.module.zookeeper.CuratorFramework
import com.soundcloud.publicApiStrangler.Routing._
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.filter._
import com.soundcloud.publicApiStrangler.support._
import com.twitter.finagle.Filter
import com.twitter.finagle.http.filter.JsonpFilter
import com.twitter.finagle.http.{Method, Request, Response}

object App {
  def main(args: Array[String]): Unit = {
    val config = new AppConfig
    val telemetry = Telemetry.defaultInstance
    val exceptionCollector = new ExceptionCollector(
      telemetry,
      airbrakeClient = Some(new AirbrakeClient(AirbrakeConfig.from(config)))
    )

    val clients = new Clients(config, telemetry)
    val handlers = new Handlers(telemetry, clients)
    val fallbackHandlerConfig = new FallbackHandlerConfiguration(telemetry, handlers.mothershipDispatcher)

    val bffApplication =
      BffApplication(Urn("soundcloud", "systems", "public-api-strangler"), config.getApplicationResourceName)

    val memcachedResourceName = ResourceName("PUBLIC_API_STRANGLER_MEMCACHED")
    val memcachedClient = RichMemcachedClient(
      MemcachedClientConfig.from(memcachedResourceName, config),
      telemetry
    )

    val curatorFramework = CuratorFramework(config, telemetry)
    val rateLimitingFacade = {
      new RateLimitingFacade(
        bffApplication,
        curatorFramework,
        clients.userAuthentication,
        config,
        telemetry,
        memcachedClient,
        clients.rolloutClient,
        Some(Seq(RateLimits.playsRateLimiter, RateLimits.searchRateLimiter))
      )
    }

    val limitOffsetPaths = Seq(
      """/e1/me/likes""",
      """/e1/me/playlist_likes""",
      """/e1/me/playlist_likes/ids""",
      """/e1/me/track_likes""",
      """/e1/me/track_likes/ids""",
      """/e1/users/\d+/likes""",
      """/e1/users/\d+/playlist_likes""",
      """/e1/users/\d+/playlist_likes/ids""",
      """/e1/users/\d+/track_likes""",
      """/e1/users/\d+/track_likes/ids""",
      """/me/favorites""",
      """/me/favorites/ids""",
      """/tracks/\d+/favoriters""",
      """/users/\d+/favorites""",
      """/users/\d+/favorites/ids"""
    )
    val limitOffset = 200

    val responseDump = new ResponseDumpSessionRegistry

    val router = HandlerRouterBuilder()
      .registerFallback(fallbackHandlerConfig.fallbackHandler)
      .register(Method.Get, rateLimitingFacade.statusEndpoint, rateLimitingFacade.rateLimitStatusHandler.handle)
      .register(
        List.concat(
          forUserFollowHandler(handlers.userFollowHandler),
          forMothershipDispatcher(handlers.mothershipDispatcher),
          forTokenExchange(handlers.tokenExchangeRolloutHandler.handle _),
          forSingleTrackHandler(handlers.singleTrackHandler),
          forPlaylistHandler(handlers.playlistsHandler),
          forSimilarSoundsHandler(handlers.similarSoundsHandler),
          forTracksHandler(handlers.tracksHandler),
          forUserRelatedMothershipDispatcher(handlers.userRelatedMothershipDispatcher),
          forSearchHandler(handlers.searchHandler),
          forUserTracksHandler(handlers.userTracksHandler),
          forRepostsHandler(handlers.repostsHandler),
          forRepostersHandler(handlers.repostersHandler),
          forTimelineHandler(handlers.timelineHandler),
          forTrackStreamsHandler(handlers.trackStreamsHandler),
          forTrackDownloadHandler(handlers.trackDownloadHandler)
        )
      )
      .build

    // IMPORTANT: the order of these filters matters a lot, be careful when adding new ones or moving things around
    val additionalFilters: List[Filter[Request, Response, Request, Response]] =
      List(
        CorsFilter((_, _) => true), // allow all CORS origins (for now)
        new CorsTelemetryFilter(telemetry, router),
        new StaticFilesFilter,
        new AcceptOnlyJsonRequestFilter(
          () => clients.rolloutClient.isActive(BasicRolloutFeature("strip_format_xml_param"))
        ),
        new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
        new CookieHeaderRemovalFilter,
        new ExceptForTrackUploadsFilter(rateLimitingFacade.filter),
        new ExceptForTrackUploadsFilter(new ContentAuthorizationFilter(clients.authorizeContent)),
        new JsonpFilter,
        new SuccesfulResponseTypeMetricFilter(telemetry)
      )

    new AdminServer(
      config = config,
      telemetry = telemetry,
      responseDumpSessionRegistry = Some(responseDump),
      customHandlers = List(
        (
          Method.Get,
          rateLimitingFacade.diagnosticsEndpoint,
          rateLimitingFacade.rateLimitingDiagnosticsAdminHandler.handle
        )
      ),
      exceptionCollector = exceptionCollector,
      applicationRouter = Some(router)
    ).start()

    BffHttpServer(
      resourceName = config.getApplicationResourceName,
      config = HttpServerConfig.from(config),
      telemetry = telemetry,
      router = router,
      customFilters = additionalFilters,
      responseDumpSessionRegistry = Some(responseDump),
      exceptionCollector = exceptionCollector
    ).start().join()
  }
}
