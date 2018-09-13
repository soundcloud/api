package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.admin.AdminServer
import com.soundcloud.jvmkit.module.bff.BffHttpServer
import com.soundcloud.jvmkit.module.bff.ratelimiting.facade._
import com.soundcloud.jvmkit.module.http.server.akira.ResponseDumpSessionRegistry
import com.soundcloud.jvmkit.module.http.server.config.HttpServerConfig
import com.soundcloud.jvmkit.module.http.server.{HandlerRouterBuilder, ResponseBuilder}
import com.soundcloud.jvmkit.module.memcached.RichMemcachedClient
import com.soundcloud.jvmkit.module.memcached.config.MemcachedClientConfig
import com.soundcloud.jvmkit.module.rollout.{BasicRolloutFeature, Rollout}
import com.soundcloud.jvmkit.module.telemetry.exceptions.{AirbrakeClient, AirbrakeConfig, ExceptionCollector}
import com.soundcloud.jvmkit.module.telemetry.{MetricsRegistry, MetricsRegistryImpl, Telemetry}
import com.soundcloud.jvmkit.module.util.config.AppConfig
import com.soundcloud.jvmkit.module.util.{ResourceName, Urn}
import com.soundcloud.jvmkit.module.zookeeper.CuratorFramework
import com.soundcloud.publicApiStrangler.Routing._
import com.soundcloud.publicApiStrangler.authorization._
import com.soundcloud.publicApiStrangler.filter.{DefaultResponseHeadersFilter, _}
import com.soundcloud.publicApiStrangler.support._
import com.twitter.finagle.SimpleFilter
import com.twitter.finagle.http.{Method, Request, Response}
import com.twitter.util.Future

object App extends Handlers with FallbackHandlerConfiguration {

  lazy val moduleConfig = new AppConfig()

  lazy val metricsRegistry: MetricsRegistry = MetricsRegistryImpl.defaultRegistry

  lazy val moduleTelemetry = new Telemetry(metricsRegistry)

  lazy val exceptionCollector = new ExceptionCollector(
    moduleTelemetry,
    airbrakeClient = Some(new AirbrakeClient(AirbrakeConfig.from(moduleConfig)))
  )

  def main(args: Array[String]): Unit = {
    val bffApplication = BffApplication(new Urn("soundcloud", "systems", "public-api-strangler"), moduleConfig.getApplicationResourceName)

    val memcachedResourceName = ResourceName("PUBLIC_API_STRANGLER_MEMCACHED")
    lazy val memcachedClient = {
      RichMemcachedClient(
        MemcachedClientConfig.from(memcachedResourceName, moduleConfig),
        moduleTelemetry)
    }

    val curatorFramework = CuratorFramework(moduleConfig, moduleTelemetry)
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

    val responseDump = new ResponseDumpSessionRegistry

    lazy val additionalFilters: List[SimpleFilter[Request, Response]] =
      List(
        new StaticFilesFilter,
        new AcceptOnlyJsonRequestFilter(() => new StripXmlRollout(rolloutClient).stripXml),
        new OffsetLimitRequestFilter(limitOffsetPaths, limitOffset),
        new CookieHeaderRemovalFilter,
        new DefaultResponseHeadersFilter,
        new OptionsRequestCacheHeadersFilter,
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
        forTimelineHandler(timelineHandler),
        forTrackStreamsHandler(trackStreamsHandler)))
      .register(Method.Get, "/-/health", (_) => Future.value(ResponseBuilder.ok()))
      .build

    new AdminServer(
      config = moduleConfig,
      telemetry = moduleTelemetry,
      responseDumpSessionRegistry = Some(responseDump),
      customHandlers = List(
        (Method.Get, rateLimitingFacade.diagnosticsEndpoint, rateLimitingFacade.rateLimitingDiagnosticsAdminHandler.handle)
      ),
      rollout = rollout,
      exceptionCollector = exceptionCollector
    ).start()


    BffHttpServer(resourceName = moduleConfig.getApplicationResourceName,
      config = HttpServerConfig.from(moduleConfig),
      telemetry = moduleTelemetry,
      router = router,
      customFilters = additionalFilters,
      responseDumpSessionRegistry = Some(responseDump),
      exceptionCollector = exceptionCollector
    ).start().join()
  }
}

class StripXmlRollout(rollout: Rollout) {
  def stripXml: Future[Boolean] = rollout.isActive(BasicRolloutFeature("strip_format_xml_param"))
}
