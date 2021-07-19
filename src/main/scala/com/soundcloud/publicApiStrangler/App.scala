package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.admin.AdminServer
import com.soundcloud.jvmkit.module.bff.BffHttpServer
import com.soundcloud.jvmkit.module.bff.filters.CorsFilter
import com.soundcloud.jvmkit.module.bff.ratelimiting.facade._
import com.soundcloud.jvmkit.module.http.server.HandlerRouterBuilder
import com.soundcloud.jvmkit.module.http.server.akira.ResponseDumpSessionRegistry
import com.soundcloud.jvmkit.module.http.server.config.HttpServerConfig
import com.soundcloud.jvmkit.module.memcached.RichMemcachedClient
import com.soundcloud.jvmkit.module.memcached.config.MemcachedClientConfig
import com.soundcloud.jvmkit.module.rollout.BasicRolloutFeature
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.util.config.AppConfig
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.jvmkit.module.zookeeper.CuratorFramework
import com.soundcloud.publicApiStrangler.Routing._
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
      telemetry
    )

    val clients = new Clients(config, telemetry, exceptionCollector)
    val handlers = new Handlers(telemetry, clients, exceptionCollector)

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
        exceptionCollector,
        Some(
          Seq(
            RateLimits.playsRateLimiter,
            RateLimits.searchRateLimiter,
            RateLimits.repostsRateLimiter,
            RateLimits.dummyRateLimiter
          )
        )
      )
    }

    val limitOffsetPaths = Seq(
      """/e1/me/track_likes""",
      """/e1/me/track_likes/ids""",
      """/e1/users/\d+/playlist_likes""",
      """/e1/users/\d+/track_likes""",
      """/me/favorites""",
      """/tracks/\d+/favoriters""",
      """/users/\d+/favorites"""
    )
    val limitOffset = 200

    val responseDump = new ResponseDumpSessionRegistry

    val router = HandlerRouterBuilder()
      .register(Method.Get, rateLimitingFacade.statusEndpoint, rateLimitingFacade.rateLimitStatusHandler.handle)
      .register(
        List.concat(
          forUserFollowHandler(handlers.userFollowHandler),
          forMothershipDispatcher(handlers.mothershipDispatcher),
          forOauthGrantExchange(handlers.oauthGrantExchangeHandler),
          forSingleTrackHandler(handlers.singleTrackHandler),
          forPlaylistHandler(handlers.playlistsHandler),
          forSimilarTracksHandler(handlers.similarTracksHandler),
          forTracksHandler(handlers.tracksHandler),
          forUserRelatedMothershipDispatcher(handlers.userRelatedMothershipDispatcher),
          forSearchHandler(handlers.searchHandler),
          forUserTracksHandler(handlers.userTracksHandler),
          forUserPlaylistsHandler(handlers.userPlaylistsHandler),
          forRepostsHandler(handlers.repostsHandler),
          forTimelineHandler(handlers.timelineHandler),
          forTrackStreamsHandler(handlers.trackStreamsHandler),
          forTrackDownloadHandler(handlers.trackDownloadHandler),
          forLikesHandler(handlers.likesHandler),
          forCommentsHandler(handlers.commentsHandler),
          forMeHandler(handlers.meHandler),
          forUsersHandler(handlers.usersHandler),
          forDummyHandler()
        )
      )
      .build

    // IMPORTANT: the order of these filters matters a lot, be careful when adding new ones or moving things around
    val additionalFilters: List[Filter[Request, Response, Request, Response]] =
      List(
        new SuccesfulResponseTypeMetricFilter(telemetry),
        new ErrorResponseTypeFilter(telemetry, router),
        new JsonpFilter,
        CorsFilter((_, _) => true), // allow all CORS origins (for now)
        new CorsTelemetryFilter(telemetry, router),
        new StaticFilesFilter,
        new ExceptForTrackUploadsFilter(
          new ClientApplicationAuthFilter(
            clients.userAuthentication,
            telemetry,
            router,
            () => clients.rolloutClient.isActive(BasicRolloutFeature("enforce_oauth_header"))
          )
        ),
        new ExceptForTrackUploadsFilter(
          new ClientApplicationActivityTelemetryFilter(clients.userAuthentication, telemetry, router)
        ),
        new AcceptOnlyJsonRequestFilter(
          () => clients.rolloutClient.isActive(BasicRolloutFeature("strip_format_xml_param"))
        ),
        new HeadRequestFilter,
        new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
        new CookieHeaderRemovalFilter,
        new ExceptForTrackUploadsFilter(rateLimitingFacade.filter),
        new DeprecatedEndpointUsageFilter(clients.userAuthentication, telemetry, router),
        new RequestTelemetryFilter(clients.userAuthentication, telemetry, router),
        new PlaylistsWithTracksTelemetryFilter(telemetry, router)
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
