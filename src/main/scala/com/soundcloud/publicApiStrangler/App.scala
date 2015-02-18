package com.soundcloud.publicApiStrangler

import com.soundcloud.bff._
import com.soundcloud.bff.authorization.ContentAuthorizationService
import com.soundcloud.bff.media.{MediaUrlsRepository, WaveformUrlsRepository}
import com.soundcloud.bff.services.JsonService
import com.soundcloud.jvmkit.config.ConfigConvention
import com.soundcloud.publicApiStrangler.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.mapper.timeline.e1.{ActivitiesMapper, StreamMapper}
import com.soundcloud.publicApiStrangler.mapper.timeline.publicApi.ActivitiesWithOriginMapper
import com.soundcloud.publicApiStrangler.mapper.timeline.{EntityMapper, EntitySummaryMapper}
import com.soundcloud.publicApiStrangler.mapper.trackstreams.{TrackStreamJsonResponseMapper, TrackStreamRedirectResponseMapper}
import com.soundcloud.publicApiStrangler.support._
import com.soundcloud.scalakit.ResourceName
import com.soundcloud.service.component._
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.ExceptionFilter

object App
  extends BffInjectionBasedApp
  with BazookaConfigComponent
  with GeoIpComponent
  with AuthenticatorComponent
  with OkidokiComponent
  with TimelineComponent
  with LieblingComponent
  with PublicApiClientComponent
  with GatekeeperComponent {

  private val userAuthentication = createUserAuthentication(authenticatorClient.cacheKeyAndSession, geoIpClient.get)

  private val moshimoshiService = JsonService(
    ServiceConfig("moshimoshi", config.get(ResourceName("MOSHIMOSHI"), ConfigConvention.BASE_URL), config)
  )
  private val okidokiService = JsonService(
    ServiceConfig("okidoki", config.get(ResourceName("OKIDOKI"), ConfigConvention.SRV_RECORD), config)
  )
  private val mediaService = JsonService(
    ServiceConfig("mediaservice", config.get(ResourceName("MEDIASERVICE"), ConfigConvention.SRV_RECORD), config)
  )

  private val authsyService = JsonService(
    ServiceConfig("authsy", config.get(ResourceName("AUTHSY"), ConfigConvention.SRV_RECORD), config)
  )

  private val contentAuthorizationService = new ContentAuthorizationService(authsyService)

  private val waveformUrlsRepo = new WaveformUrlsRepository(okidokiService, mediaService)
  private val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, userAuthentication, waveformUrlsRepo)

  private val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)

  private val timelineController = {
    val baseUrl = config.get("APP_BASE_URL", true)
    val entitySummaryMapper = new EntitySummaryMapper(okidokiClient, baseUrl)
    val entityMapper = new EntityMapper(okidokiClient, lieblingClient, baseUrl, entitySummaryMapper)
    val streamMapper = new StreamMapper(timelineClient, entityMapper, entitySummaryMapper)
    val activitiesMapper = new ActivitiesMapper(timelineClient, entityMapper, entitySummaryMapper)
    val publicActivitiesMapper = new ActivitiesWithOriginMapper(timelineClient, entityMapper, entitySummaryMapper)
    val pagination = new CursorPagination(baseUrl)
    new TimelineController(userAuthentication, streamMapper, activitiesMapper, publicActivitiesMapper, pagination)
  }

  private val groupController = {
    val forwardHandler = new ForwardRequestHandler(publicApiClient)
    new GroupController(userAuthentication, gatekeeperClient, forwardHandler)
  }

  private val trackStreamsController = {
    val trackStreamUrlToJsonResponseMapper = new TrackStreamJsonResponseMapper
    val trackStreamUrlToRedirectMapper = new TrackStreamRedirectResponseMapper

    val mediaUrlsRepository = new MediaUrlsRepository(okidokiService, mediaService)
    val trackStreamSnipHandler = new TrackStreamSnipHandler(mothershipDispatcher, contentAuthorizationService, mediaUrlsRepository)
    new TrackStreamsController(
      userAuthentication,
      trackStreamUrlToJsonResponseMapper,
      trackStreamUrlToRedirectMapper,
      mothershipDispatcher,
      trackStreamSnipHandler)
  }

  private val userFollowController = new UserFollowController(userAuthentication, mothershipDispatcher, moshimoshiService)

  override val fallbackHandler = Some(mothershipDispatcher)

  override val customFilters = List(
    new ExceptionFilter[Request],
    new AcceptOnlyJsonRequestFilter(Set("/crossdomain.xml")),
    new ContentAuthorizationFilter(authorizeContent)
  )

  override val controllers = List(
    timelineController,
    groupController,
    trackStreamsController,
    userFollowController
  )
}
