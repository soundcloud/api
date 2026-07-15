package com.soundcloud.apipublic

import com.soundcloud.apipublic.handler._
import com.soundcloud.apipublic.handler.comments.CommentsHandler
import com.soundcloud.apipublic.handler.muzooka.ImageDownloader
import com.soundcloud.apipublic.handler.search.SearchHandler
import com.soundcloud.apipublic.service.media.DownloadService
import com.soundcloud.apipublic.support.oauth.{ForwardToSecureFilter, GrantExchangeRequestParser, RailsLikeParamsParser}
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector

class Handlers(
    telemetry: Telemetry,
    clients: Clients,
    exceptionCollector: ExceptionCollector
) {
  import clients._

  val timelineHandler: TimelineHandler = new TimelineHandler(
    userAuthentication,
    timelineService,
    baseUrl
  )

  val trackStreamsHandler: TrackStreamsHandler = new TrackStreamsHandler(
    userAuthentication,
    streamService,
    trackAccessRecorderService
  )

  val trackDownloadHandler: TrackDownloadHandler = new TrackDownloadHandler(
    userAuthentication,
    new DownloadService(tracksMediaTwirpClient)
  )

  val tracksHandler = new TracksHandler(
    userAuthentication,
    trackCoordinatorClient,
    trackUpdateService,
    exceptionCollector
  )

  val singleTrackHandler =
    new SingleTrackHandler(userAuthentication, tracksService)

  val storefrontHandler =
    new StorefrontHandler(userAuthentication, storefrontService)

  val userTracksHandler = new UserTracksHandler(
    userAuthentication,
    userTracksService,
    baseUrl
  )

  val userFollowHandler =
    new UserFollowHandler(userAuthentication, okidokiClient, followsClient, userRepresentationsService, baseUrl)

  val searchHandler: SearchHandler = {
    new SearchHandler(
      userAuthentication,
      baseUrl,
      searchService,
      telemetry
    )
  }

  val similarTracksHandler: SimilarTracksHandler = {
    new SimilarTracksHandler(
      userAuthentication,
      similarTracksService,
      baseUrl
    )
  }

  val relatedArtistsHandler: RelatedArtistsHandler =
    new RelatedArtistsHandler(userAuthentication, relatedArtistsService, baseUrl)

  val playlistsHandler =
    new PlaylistsHandler(
      userAuthentication,
      playlistDeletionClient,
      playlistService,
      baseUrl,
      exceptionCollector
    )

  val userPlaylistsHandler = new UserPlaylistsHandler(userAuthentication, userPlaylistsService, baseUrl)

  val repostsHandler = new RepostsHandler(userAuthentication, repostsService, baseUrl)

  val likesHandler = new LikesHandler(userAuthentication, likesService, userRepresentationsService, baseUrl)

  val grantExchangeRequestMapper = new GrantExchangeRequestParser(new RailsLikeParamsParser())
  val forwardToSecureFilter =
    new ForwardToSecureFilter(grantExchangeRequestMapper, clients.rolloutClient, clients.secureClient, telemetry)

  val oauthGrantExchangeHandler =
    new OauthGrantExchangeHandler(
      telemetry,
      grantExchangeRequestMapper.parse,
      grantExchangeService
    )

  val commentsHandler = new CommentsHandler(userAuthentication, commentsService, baseUrl)

  val meHandler = new MeHandler(userAuthentication, meService)

  val recentlyPlayedHandler =
    new RecentlyPlayedHandler(userAuthentication, recentlyPlayedService)

  val usersHandler = new UsersHandler(userAuthentication, userRepresentationsService)

  val resolveHandler = new ResolveHandler(userAuthentication, resolveService)

  val connectHandler = new ConnectHandler()

  val webProfilesHandler = new WebProfilesHandler(userAuthentication, moshimoshiClient)

  val clientApplicationsHandler = new ClientApplicationsHandler(
    userAuthentication,
    clientApplicationsClient,
    clientApplicationMetadataClient,
    creatorSubscriptionsClient,
    exceptionCollector
  )

  val imageDownloader = new ImageDownloader()

  val muzookaWebhookHandler =
    new MuzookaWebhookHandler(
      muzookaApiKey,
      hocuspocusClient,
      moshimoshiClient,
      userAuthentication,
      tokenDispenserClient,
      imageDownloader
    )
}
