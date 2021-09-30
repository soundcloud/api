package com.soundcloud.publicApiStrangler

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
import com.soundcloud.publicApiStrangler.client._
import com.soundcloud.publicApiStrangler.client.comments.MoshimoshiCommentsClient
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCountsClient
import com.soundcloud.publicApiStrangler.client.follows.FollowsClient
import com.soundcloud.publicApiStrangler.client.liebling.LieblingClient
import com.soundcloud.publicApiStrangler.client.media.TrackAccessRecorderClient
import com.soundcloud.publicApiStrangler.client.mothership.{MoshimoshiClient, OkidokiClient, RichOkidokiClient}
import com.soundcloud.publicApiStrangler.client.playlists.PlaylistDeletionClient
import com.soundcloud.publicApiStrangler.client.reposts.RepostsClient
import com.soundcloud.publicApiStrangler.client.search.SearchClient
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorClient
import com.soundcloud.publicApiStrangler.client.trackmetadata.TrackmetadataClient
import com.soundcloud.publicApiStrangler.service._
import com.soundcloud.publicApiStrangler.service.comments.CommentService
import com.soundcloud.publicApiStrangler.service.media.{StreamService, TrackAccessRecorderService}
import com.soundcloud.publicApiStrangler.service.oauth.GrantExchangeService
import com.soundcloud.publicApiStrangler.service.resolve.ResolveService
import com.soundcloud.publicApiStrangler.service.trackrepresentation.{TrackRepresentationsService, TrackUpdateService}
import com.soundcloud.publicApiStrangler.service.tracks.VisibleTrackMapper
import com.soundcloud.publicApiStrangler.service.users.{MeService, UserRepresentationsService}
import com.soundcloud.publicApiStrangler.subscriptions.SubmarineClient
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.RetryPolicy.RetryableWriteException
import com.twitter.util.{Throw, Try}
import proto.soundcloud.authenticator.access_grant_exchange.AccessGrantExchangeClientProtobuf
import proto.soundcloud.playlists.api.{PlaylistsClientProtobuf, WritesClientProtobuf}
import proto.soundcloud.tracks.api.{
  LikesClientProtobuf,
  MediaClientProtobuf,
  RepostsClientProtobuf,
  TrackMetadataClientProtobuf
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

  val publicApiClient: Service[Request, Response] = {
    val name = ResourceName("PUBLIC_API")

    val writeExceptions: PartialFunction[(Request, Try[Response]), Boolean] = {
      case (_, Throw(RetryableWriteException(_))) => true
    }

    HttpClient[String](
      HttpClientConfig.from(name, config),
      telemetry,
      retryOn = Some(writeExceptions)
    ).httpService
  }

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
      exceptionCollector
    )
  val likesService = new LikesService(tracksService, playlistService, lieblingClient, likeTracksTwirpClient)
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
  val commentsService =
    new CommentService(richOkidokiClient, moshimoshiClient, moshimoshiCommentsClient)

  private val oauthGrantExchangeClient = TwirpClient(
    ResourceName("oauth_authorization"),
    config,
    telemetry,
    new AccessGrantExchangeClientProtobuf(_, _)
  )
  val grantExchangeService = new GrantExchangeService(oauthGrantExchangeClient)

  val resolveService = new ResolveService(moshimoshiClient, trackVisibilityService, playlistService, baseUrl)
}
