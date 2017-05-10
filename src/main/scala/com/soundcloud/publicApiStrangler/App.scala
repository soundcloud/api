package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.config.AppConfig
import com.soundcloud.jvmkit.module.admin.AdminServer
import com.soundcloud.jvmkit.module.bff.BffHttpServer
import com.soundcloud.jvmkit.module.bff.filters.SessionCacheFilter
import com.soundcloud.jvmkit.module.bff.ratelimiting.facade._
import com.soundcloud.jvmkit.module.http.server.config.HttpServerConfig
import com.soundcloud.jvmkit.module.http.server.{HandlerRouterBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.memcached.MemcachedClient
import com.soundcloud.jvmkit.module.memcached.config.MemcachedClientConfig
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.telemetry.{Telemetry => ModuleTelemetry}
import com.soundcloud.jvmkit.module.util.config.{AppConfig => ModuleAppConfig}
import com.soundcloud.jvmkit.module.util.{ResourceName => ModuleResourceName, Urn => ModuleUrn}
import com.soundcloud.jvmkit.module.zookeeper.CuratorFrameworkFactory
import com.soundcloud.jvmkit.telemetry.{MetricsRegistryImpl, Telemetry}
import com.soundcloud.publicApiStrangler.Routing._
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.headers.DefaultResponseHeadersFilter
import com.soundcloud.publicApiStrangler.support._
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future

object App extends Handlers with FallbackHandlerConfiguration {

  def config = new AppConfig()

  def moduleConfig = new ModuleAppConfig()

  def metricsRegistry = MetricsRegistryImpl.defaultRegistry

  def telemetry = new Telemetry(config, metricsRegistry)

  def moduleTelemetry = new ModuleTelemetry(config.getApplicationName, metricsRegistry)

  def main(args: Array[String]): Unit = {

    val bffApplication = BffApplication(new ModuleUrn("soundcloud", "systems", "public-api-strangler"), moduleConfig.getApplicationResourceName)

    val memcachedResourceName = ModuleResourceName("MEMCACHED")
    lazy val memcachedClient = {
      MemcachedClient(
        MemcachedClientConfig.from(memcachedResourceName, moduleConfig),
        moduleTelemetry)
    }

    val curatorFramework = CuratorFrameworkFactory.create(moduleConfig, moduleTelemetry)
    val rateLimitingFacade = {
      new RateLimitingFacade(
        bffApplication,
        curatorFramework,
        userAuthentication,
        moduleConfig,
        moduleTelemetry,
        memcachedClient,
        rolloutClient,
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

    lazy val additionalFilters: List[SimpleFilter[Request, Response]] =
      List(
        new StaticFilesFilter,
        new AcceptOnlyJsonRequestFilter(() => new StripXmlRollout(rolloutClient).stripXml),
        new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
        new CookieHeaderRemovalFilter,
        new SessionCacheFilter(userAuthentication),
        new DefaultResponseHeadersFilter,
        new ExceptForTrackUploadsFilter(rateLimitingFacade.filter),
        new ExceptForTrackUploadsFilter(new ContentAuthorizationFilter(authorizeContent)),
        new SuccesfulResponseTypeMetricFilter(moduleTelemetry)
      )

    val router = HandlerRouterBuilder()
      .registerFallback(fallbackHandler)
      .register(Method.Get, rateLimitingFacade.statusEndpoint, rateLimitingFacade.rateLimitStatusHandler.handle)
      .register(List.concat(
        forUserFollowHandler(userFollowHandler),
        forMothershipDispatcher(mothershipDispatcher),
        forSingleTrackHandler(singleTrackHandler),
        forPlaylistHandler(playlistsHandler),
        forSimilarSoundsHandler(similarSoundsHandler),
        forTracksHandler(tracksHandler),
        forUserRelatedMothershipDispatcher(userRelatedMothershipDispatcher),
        forSearchHandler(searchHandler),
        forUserTracksHandler(userTracksHandler),
        forRepostsHandler(repostsHandler),
        forRepostersHandler(repostersHandler),
        forSpamWarningsHandler(spamWarningsHandler),
        forTimelineHandler(timelineHandler),
        forTrackStreamsHandler(trackStreamsHandler)))
      .register(Method.Get, "/-/health", (_) => Future.value(ResponseBuilder.ok()))
      .build


    new AdminServer(
      config = moduleConfig,
      telemetry = moduleTelemetry,
      customHandlers = List(
        (Method.Get, rateLimitingFacade.diagnosticsEndpoint, rateLimitingFacade.rateLimitingDiagnosticsAdminHandler.handle _)
      ),
      rollout = rollout
    ).start()

    BffHttpServer(resourceName = moduleConfig.getApplicationResourceName,
      config = HttpServerConfig.from(moduleConfig),
      telemetry = moduleTelemetry,
      router = router,
      customFilters = additionalFilters
    ).start().join()
  }
}

class StripXmlRollout(rollout: Rollout) {
  def stripXml: Future[Boolean] = rollout.isActive(BasicRolloutFeature("strip_format_xml_param"))
}
