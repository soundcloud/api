package com.soundcloud.apipublic

import com.soundcloud.apipublic.client._
import com.soundcloud.apipublic.client.comments.CommentsTwirpClient
import com.soundcloud.apipublic.client.followcounts.{FollowCountsClient, FollowsCountsTwirpClient}
import com.soundcloud.apipublic.client.follows.FollowsClient
import com.soundcloud.apipublic.client.media.TrackAccessRecorderClient
import com.soundcloud.apipublic.client.mothership.{MoshimoshiClient, OkidokiClient, RichOkidokiClient}
import com.soundcloud.apipublic.client.playlists.PlaylistDeletionClient
import com.soundcloud.apipublic.client.reposts.RepostsClient
import com.soundcloud.apipublic.client.search.SearchApiClient
import com.soundcloud.apipublic.client.shortlinks.ShortLinksClient
import com.soundcloud.apipublic.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.apipublic.client.trackmetadata.TrackmetadataClient
import com.soundcloud.apipublic.client.tracks.TracksTwirpClient
import com.soundcloud.apipublic.service._
import com.soundcloud.apipublic.service.comments.CommentService
import com.soundcloud.apipublic.service.media.{StreamService, TrackAccessRecorderService}
import com.soundcloud.apipublic.service.oauth.GrantExchangeService
import com.soundcloud.apipublic.service.resolve.ResolveService
import com.soundcloud.apipublic.service.trackrepresentation.{
  LikedTracksService,
  TrackRepresentationsService,
  TrackUpdateService
}
import com.soundcloud.apipublic.service.tracks.VisibleTrackMapper
import com.soundcloud.apipublic.service.users.{MeService, UserRepresentationsService}
import com.soundcloud.apipublic.subscriptions.SubmarineClient
import com.soundcloud.hocuspocus.HocuspocusClientProtobuf
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{DynamicHttpClient, HttpClient, JsonClient}
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.TwirpClient
import com.soundcloud.jvmkit.module.twirp.filters.ClientTelemetry
import com.soundcloud.jvmkit.module.util.ResourceName
import com.soundcloud.jvmkit.module.util.config.{AppConfig, DataSensitivity}
import proto.soundcloud.authenticator.access_grant_exchange.AccessGrantExchangeClientProtobuf
import proto.soundcloud.comments.api.CommentsClientProtobuf
import proto.soundcloud.follows.api.FollowsClientProtobuf
import proto.soundcloud.likes.{api => likes}
import proto.soundcloud.playlists.api.{
  PlaylistsClientProtobuf,
  WritesClientProtobuf,
  LikesClientProtobuf => PlaylistLikesClientProtobuf
}
import proto.soundcloud.search.api.SearchClientProtobuf
import proto.soundcloud.tracks.api.{
  MediaClientProtobuf,
  RepostsClientProtobuf,
  TrackMetadataClientProtobuf,
  CommentsClientProtobuf => TrackCommentsClientProtobuf,
  LikesClientProtobuf => TrackLikesClientProtobuf
}

class Clients(
    config: AppConfig,
    telemetry: Telemetry,
    exceptionCollector: ExceptionCollector
) {
  private def jsonClient(resourceName: String) = JsonClient(
    HttpClientConfig.from(ResourceName(resourceName), config),
    telemetry
  )

  private val okidokiJsonClient = jsonClient("okidoki")
  val okidokiClient = new OkidokiClient(okidokiJsonClient, exceptionCollector)

  val userAuthentication = UserAuthentication(config, telemetry)

  val baseUrl: String = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  val rolloutClient = Rollout(config, telemetry)
  val rollout = Some(rolloutClient)

  val timelineClient = new TimelineJsonClient(jsonClient("timeline"))

  val searchClient = new SearchApiClient(
    TwirpClient(
      ResourceName("searchsdui"),
      config,
      telemetry,
      new SearchClientProtobuf(_, _)
    ),
    exceptionCollector
  )

  val followsClient = new FollowsClient(jsonClient("follows"))

  val repostsClient = new RepostsClient(jsonClient("reposts"))

  val systemPlaylistsClient = new SystemPlaylistsClient(jsonClient("system_playlists"))

  val trackCoordinatorClient = new TrackCoordinatorClient(jsonClient("track_coordinator"))

  lazy val moshimoshiClient = new MoshimoshiClient(jsonClient("moshimoshi"), exceptionCollector)

  private val followsCountsProtoClient = TwirpClient(
    ResourceName("follows"),
    config,
    telemetry,
    new FollowsClientProtobuf(_, _)
  )

  private val followsCountsTwirpClient = new FollowsCountsTwirpClient(followsCountsProtoClient)

  val followCountsClient = new FollowCountsClient(followsCountsTwirpClient, config)

  val trackmetadataClient = new TrackmetadataClient(jsonClient("trackmetadata"))

  lazy val shortLinksClient = new ShortLinksClient(
    new DynamicHttpClient(HttpClientConfig.from(ResourceName("dynamic_http"), config), telemetry)
  )

  val tracksMediaTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new MediaClientProtobuf(_, _)
  )

  // TrackMetadata service from the Tracks VAS
  val trackMetadataTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new TrackMetadataClientProtobuf(_, _)
  )

  val trackRepostsTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new RepostsClientProtobuf(_, _)
  )

  val likeTracksTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new TrackLikesClientProtobuf(_, _)
  )

  val likesTwirpClient = TwirpClient(
    ResourceName("likes"),
    config,
    telemetry,
    new likes.LikesClientProtobuf(_, _)
  )

  val likesPlaylistsTwirpClient = TwirpClient(
    ResourceName("playlists"),
    config,
    telemetry,
    new PlaylistLikesClientProtobuf(_, _)
  )

  val playlistsTwirpClient = TwirpClient(
    ResourceName("playlists"),
    config,
    telemetry,
    new PlaylistsClientProtobuf(_, _)
  )

  val playlistsWritesTwirpClient = TwirpClient(
    ResourceName("playlists"),
    config,
    telemetry,
    new WritesClientProtobuf(_, _)
  )

  val trackAccessRecorderService =
    new TrackAccessRecorderService(new TrackAccessRecorderClient(jsonClient("track_access_recorder")))

  val richOkidokiClient = new RichOkidokiClient(okidokiJsonClient, exceptionCollector)

  val trackVisibilityService =
    new TrackVisibilityService(
      trackMetadataTwirpClient,
      new VisibleTrackMapper
    )

  val likedTracksService = new LikedTracksService(likesTwirpClient, exceptionCollector)

  val tracksService = new TrackRepresentationsService(
    trackVisibilityService,
    richOkidokiClient,
    likedTracksService
  )

  private val hocuspocusConfig = HttpClientConfig.from(ResourceName("hocuspocus"), config)
  private val hocuspocusHttpClient = HttpClient[String](hocuspocusConfig, telemetry)
  private val hocuspocusTelemetry = ClientTelemetry.from(hocuspocusConfig, telemetry)
  val hocuspocusClient = new HocuspocusClientProtobuf(hocuspocusHttpClient.httpService, hocuspocusTelemetry)

  val userTracksService = new UserTracksService(tracksService, trackmetadataClient)
  val trackUpdateService =
    new TrackUpdateService(trackCoordinatorClient, okidokiClient, hocuspocusClient, tracksService)
  val similarTracksService = new SimilarTracksService(tracksService, systemPlaylistsClient)
  val playlistService =
    new PlaylistsService(
      playlistsTwirpClient,
      tracksService,
      okidokiClient,
      playlistsWritesTwirpClient,
      exceptionCollector,
      hocuspocusClient
    )
  val likesService =
    new LikesService(
      tracksService,
      playlistService,
      likesTwirpClient,
      likeTracksTwirpClient,
      likesPlaylistsTwirpClient
    )
  val userPlaylistsService = new UserPlaylistsService(playlistService, okidokiClient)

  private val submarineClient = new SubmarineClient(jsonClient("submarine"))

  val userRepresentationsService =
    new UserRepresentationsService(
      followCountsClient,
      repostsClient,
      okidokiClient,
      likesTwirpClient,
      submarineClient,
      exceptionCollector
    )

  val meService = new MeService(userRepresentationsService, okidokiClient, trackCoordinatorClient, exceptionCollector)

  val searchService =
    new SearchService(
      searchClient,
      tracksService,
      playlistService,
      userRepresentationsService
    )

  val timelineService = new TimelineService(timelineClient, okidokiClient, tracksService, playlistService)

  val streamService = new StreamService(trackVisibilityService, tracksMediaTwirpClient)
  val repostsService =
    new RepostsService(userRepresentationsService, repostsClient, trackRepostsTwirpClient)
  val playlistDeletionClient = new PlaylistDeletionClient(okidokiJsonClient)

  val trackCommentsTwirpClient = TwirpClient(
    ResourceName("tracks"),
    config,
    telemetry,
    new TrackCommentsClientProtobuf(_, _)
  )

  val commentsTwirpClient = TwirpClient(
    ResourceName("comments"),
    config,
    telemetry,
    new CommentsClientProtobuf(_, _)
  )

  val commentsService =
    new CommentService(
      richOkidokiClient,
      new TracksTwirpClient(trackCommentsTwirpClient),
      new CommentsTwirpClient(commentsTwirpClient)
    )

  private val oauthGrantExchangeClient = TwirpClient(
    ResourceName("oauth_authorization"),
    config,
    telemetry,
    new AccessGrantExchangeClientProtobuf(_, _)
  )
  val grantExchangeService = new GrantExchangeService(oauthGrantExchangeClient)

  val resolveService =
    new ResolveService(moshimoshiClient, shortLinksClient, trackVisibilityService, playlistService, baseUrl)

  val muzookaApiKey = config.get("MUZOOKA_API_KEY", DataSensitivity.SENSITIVE)

  val tokenDispenserClient: TokenDispenserClient = {
    val client =
      JsonClient(HttpClientConfig.from(ResourceName("AUTHENTICATOR_DISPENSER"), config), telemetry)
    new TokenDispenserClient(client)
  }
}
