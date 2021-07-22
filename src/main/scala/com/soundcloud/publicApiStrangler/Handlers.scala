package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.publicApiStrangler.handler._
import com.soundcloud.publicApiStrangler.handler.comments.CommentsHandler
import com.soundcloud.publicApiStrangler.handler.search.SearchHandler
import com.soundcloud.publicApiStrangler.service.media.DownloadService
import com.soundcloud.publicApiStrangler.support.oauth.{RailsLikeParamsParser, GrantExchangeRequestParser}

class Handlers(
    telemetry: Telemetry,
    clients: Clients,
    exceptionCollector: ExceptionCollector
) {
  import clients._
  val mothershipDispatcher =
    new DispatchToMothershipHandler(userAuthentication, publicApiClient)

  val timelineHandler: TimelineHandler = new TimelineHandler(
    userAuthentication,
    timelineService
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
    trackUpdateService
  )

  val singleTrackHandler =
    new SingleTrackHandler(userAuthentication, tracksService)

  val userTracksHandler = new UserTracksHandler(
    userAuthentication,
    userTracksService
  )

  val userFollowHandler =
    new UserFollowHandler(userAuthentication, okidokiClient, followsClient, followCountsClient, repostsClient, baseUrl)

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

  val playlistsHandler =
    new PlaylistsHandler(userAuthentication, playlistDeletionClient, playlistService)

  val userPlaylistsHandler = new UserPlaylistsHandler(userAuthentication, userPlaylistsService)

  val repostsHandler = new RepostsHandler(userAuthentication, repostsService)

  val likesHandler = new LikesHandler(userAuthentication, likesService, userRepresentationsService)

  val grantExchangeRequestMapper = new GrantExchangeRequestParser(new RailsLikeParamsParser())
  val oauthGrantExchangeHandler =
    new OauthGrantExchangeHandler(
      telemetry,
      grantExchangeRequestMapper.parse,
      grantExchangeService
    )

  val commentsHandler = new CommentsHandler(userAuthentication, commentsService)

  val meHandler = new MeHandler(userAuthentication, meService)

  val usersHandler = new UsersHandler(userAuthentication, userRepresentationsService)
}
