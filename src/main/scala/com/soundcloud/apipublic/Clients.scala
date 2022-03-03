package com.soundcloud.apipublic

import com.soundcloud.hocuspocus.HocuspocusClientProtobuf
import com.soundcloud.jvmkit.module.bff.session.UserAuthentication
import com.soundcloud.jvmkit.module.http.client.config.HttpClientConfig
import com.soundcloud.jvmkit.module.http.client.{HttpClient, JsonClient}
import com.soundcloud.jvmkit.module.rollout.Rollout
import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.telemetry.exceptions.ExceptionCollector
import com.soundcloud.jvmkit.module.twirp.TwirpClient
import com.soundcloud.jvmkit.module.twirp.filters.ClientTelemetry
import com.soundcloud.jvmkit.module.util.ResourceName
import com.soundcloud.jvmkit.module.util.config.{AppConfig, DataSensitivity}
import com.soundcloud.apipublic.client._
import com.soundcloud.apipublic.client.followcounts.FollowCountsClient
import com.soundcloud.apipublic.client.follows.FollowsClient
import com.soundcloud.apipublic.client.liebling.LieblingClient
import com.soundcloud.apipublic.client.media.TrackAccessRecorderClient
import com.soundcloud.apipublic.client.moshimoshicomments.MoshimoshiCommentsClient
import com.soundcloud.apipublic.client.mothership.{MoshimoshiClient, OkidokiClient, RichOkidokiClient}
import com.soundcloud.apipublic.client.playlists.PlaylistDeletionClient
import com.soundcloud.apipublic.client.reposts.RepostsClient
import com.soundcloud.apipublic.client.search.SearchClient
import com.soundcloud.apipublic.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.apipublic.client.trackmetadata.TrackmetadataClient
import com.soundcloud.apipublic.service._
import com.soundcloud.apipublic.service.comments.CommentService
import com.soundcloud.apipublic.service.media.{StreamService, TrackAccessRecorderService}
import com.soundcloud.apipublic.service.oauth.GrantExchangeService
import com.soundcloud.apipublic.service.resolve.ResolveService
import com.soundcloud.apipublic.service.trackrepresentation.{TrackRepresentationsService, TrackUpdateService}
import com.soundcloud.apipublic.service.tracks.VisibleTrackMapper
import com.soundcloud.apipublic.service.users.{MeService, UserRepresentationsService}
import com.soundcloud.apipublic.subscriptions.SubmarineClient
import proto.soundcloud.authenticator.access_grant_exchange.AccessGrantExchangeClientProtobuf
import proto.soundcloud.comments.api.CommentsClientProtobuf
import proto.soundcloud.playlists.api.{PlaylistsClientProtobuf, WritesClientProtobuf}
import proto.soundcloud.likes.{api => likes}
import proto.soundcloud.tracks.api.{
  LikesClientProtobuf,
  MediaClientProtobuf,
  RepostsClientProtobuf,
  TrackMetadataClientProtobuf,
  CommentsClientProtobuf => TrackCommentsClientProtobuf
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

  val timelineClient = new TimelineJsonClient(jsonClient("timeline"))

  val lieblingClient = new LieblingClient(jsonClient("liebling"), exceptionCollector)

  val searchJsonClient = jsonClient("search")
  val searchClient = new SearchClient(searchJsonClient)

  val followsClient = new FollowsClient(jsonClient("follows"))

  val repostsClient = new RepostsClient(jsonClient("reposts"))

  val gatekeeperClient = new GatekeeperClient(jsonClient("gatekeeper"))

  val systemPlaylistsClient = new SystemPlaylistsClient(jsonClient("system_playlists"))

  val trackCoordinatorClient = new TrackCoordinatorClient(jsonClient("track_coordinator"))

  lazy val moshimoshiCommentsClient = new MoshimoshiCommentsClient(jsonClient("moshimoshi_comments"))
  lazy val moshimoshiClient = new MoshimoshiClient(jsonClient("moshimoshi"), exceptionCollector)

  private val stitch4followsService = jsonClient("stitch4follows")

  val followCountsClient = new FollowCountsClient(stitch4followsService, config)

  val trackmetadataClient = new TrackmetadataClient(jsonClient("trackmetadata"))

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
    new LikesClientProtobuf(_, _)
  )

  val likesTwirpClient = TwirpClient(
    ResourceName("likes"),
    config,
    telemetry,
    new likes.LikesClientProtobuf(_, _)
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

  val userAuthentication = UserAuthentication(config, telemetry)

  val baseUrl: String = config.get("APP_BASE_URL", DataSensitivity.NON_SENSITIVE)

  val rolloutClient = Rollout(config, telemetry)
  val rollout = Some(rolloutClient)

  val richOkidokiClient = new RichOkidokiClient(okidokiJsonClient, exceptionCollector)

  val trackVisibilityService =
    new TrackVisibilityService(
      trackMetadataTwirpClient,
      new VisibleTrackMapper
    )

  val tracksService = new TrackRepresentationsService(
    trackVisibilityService,
    richOkidokiClient,
    lieblingClient
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
      lieblingClient,
      playlistsWritesTwirpClient,
      exceptionCollector,
      hocuspocusClient,
      rollout = rolloutClient
    )
  val likesService =
    new LikesService(tracksService, playlistService, lieblingClient, likesTwirpClient, likeTracksTwirpClient)
  val userPlaylistsService = new UserPlaylistsService(playlistService, okidokiClient)

  private val submarineClient = new SubmarineClient(jsonClient("submarine"))

  val userRepresentationsService =
    new UserRepresentationsService(
      followCountsClient,
      repostsClient,
      okidokiClient,
      lieblingClient,
      submarineClient
    )

  val meService = new MeService(userRepresentationsService, okidokiClient, trackCoordinatorClient, exceptionCollector)

  val searchService =
    new SearchService(searchClient, tracksService, playlistService, userRepresentationsService)

  val timelineService = new TimelineService(timelineClient, tracksService, playlistService)

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
      moshimoshiClient,
      moshimoshiCommentsClient,
      trackCommentsTwirpClient,
      commentsTwirpClient,
      rolloutClient
    )

  private val oauthGrantExchangeClient = TwirpClient(
    ResourceName("oauth_authorization"),
    config,
    telemetry,
    new AccessGrantExchangeClientProtobuf(_, _)
  )
  val grantExchangeService = new GrantExchangeService(oauthGrantExchangeClient)

  val resolveService = new ResolveService(moshimoshiClient, trackVisibilityService, playlistService, baseUrl)
}
