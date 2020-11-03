package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.http.server.{Handler, JsonResponseBuilder}
import com.soundcloud.publicApiStrangler.handler.search.SearchHandler
import com.soundcloud.publicApiStrangler.handler.{DispatchToMothershipHandler, _}
import com.twitter.finagle.http.Method
import com.twitter.util.Future

object Routing {
  val tokenExchangePath = "/oauth2/token"

  // Mothership routes accept the /v1 suffix
  // See https://github.com/soundcloud/soundcloud/blob/master/lib/rack/extract_api_version.rb
  def route(method: Method, path: String, handler: Handler): List[(Method, String, Handler)] = {
    if (method == Method.Get)
      withRoutingSuffixes(method, path, handler) :::
        withRoutingSuffixes(method, "/v1" + path, handler) :::
        withRoutingSuffixes(Method.Head, path, handler) :::
        withRoutingSuffixes(Method.Head, "/v1" + path, handler)
    else
      withRoutingSuffixes(method, path, handler) :::
        withRoutingSuffixes(method, "/v1" + path, handler)
  }

  // Generates a list of pairs of a route with each possible path combination:
  // (Method.Get, "/tracks", handler)
  // (Method.Get, "/tracks/", handler)
  // (Method.Get, "/tracks.json", handler)
  // (Method.Get, "/tracks.json/", handler)
  // This is because routes in Mothership are defined with the ".:format" suffix
  // See https://github.com/soundcloud/soundcloud/blob/master/config/routes.rb
  private def withRoutingSuffixes(method: Method, path: String, handler: Handler): List[(Method, String, Handler)] = {
    List(
      (method, path, handler),
      (method, path + "/", handler),
      (method, path + ".json", handler),
      (method, path + ".json/", handler)
    )
  }

  def forUserFollowHandler(userFollowHandler: UserFollowHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/users/:id/followings", userFollowHandler.fetchFollowingsWithoutAuth) :::
      route(Method.Get, "/users/:id/followers", userFollowHandler.fetchFollowersWithoutAuth) :::
      route(Method.Get, "/users/:id/followers/:other_id", userFollowHandler.fetchPossibleFollowerWithoutAuth) :::
      route(Method.Get, "/users/:id/followings/:other_id", userFollowHandler.fetchPossibleFollowingWithoutAuth) :::
      route(Method.Get, "/me/followings", userFollowHandler.fetchFollowings) :::
      route(Method.Get, "/me/followers", userFollowHandler.fetchMyFollowers) :::
      route(Method.Get, "/me/followers/:other_id", userFollowHandler.fetchPossibleFollower) :::
      route(Method.Get, "/me/followings/:other_id", userFollowHandler.fetchPossibleFollowing) :::
      route(Method.Post, "/me/followings/:other_id", userFollowHandler.follow) :::
      route(Method.Put, "/me/followings/:other_id", userFollowHandler.follow) :::
      route(Method.Delete, "/me/followings/:other_id", userFollowHandler.unfollow)
  }

  def forMothershipDispatcher(mothershipDispatcher: DispatchToMothershipHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/connect", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/e1/me/playlist_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/e1/me/track_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/e1/me/track_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/comments", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/connections", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/connections/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/email", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/playlists/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/resolve", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/tracks/:trackId/comments", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/tracks/:trackId/comments", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/tracks/:trackId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/tracks/:trackId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/users/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/favorites/ids", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/web-profiles", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/web-profiles", mothershipDispatcher.dispatch)
  }

  def forTokenExchange(handler: Handler): List[(Method, String, Handler)] = {
    route(Method.Post, tokenExchangePath, handler)
  }

  def forSingleTrackHandler(singleTrackHandler: SingleTrackHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId", singleTrackHandler.renderTrack)
  }

  def forPlaylistHandler(playlistsHandler: PlaylistsHandler): List[(Method, String, Handler)] = {
    route(Method.Delete, "/playlists/:id", playlistsHandler.handleDelete) :::
      route(Method.Get, "/playlists/:id", playlistsHandler.handleFetchPlaylist) :::
      route(Method.Get, "/playlists/:id/tracks", playlistsHandler.handleFetchPlaylistTracks)
  }

  def forSimilarTracksHandler(similarTracksHandler: SimilarTracksHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/related", similarTracksHandler.handleSimilarTracks)
  }

  def forTracksHandler(tracksHandler: TracksHandler): List[(Method, String, Handler)] = {
    route(Method.Put, "/tracks/:trackId", tracksHandler.handleUpdateTrack) :::
      route(Method.Post, "/tracks", tracksHandler.handleCreate) :::
      route(Method.Delete, "/tracks/:trackId", tracksHandler.handleDelete)
  }

  def forUserRelatedMothershipDispatcher(
      userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher
  ): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:id/favoriters", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/tracks/:id/favoriters/:user_id", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/users/:id", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/users/:id/comments", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/me", userRelatedMothershipDispatcher.dispatchToMothership)
  }

  def forSearchHandler(searchHandler: SearchHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks", searchHandler.searchTracks) :::
      route(Method.Get, "/users", searchHandler.dispatchUserRequest) :::
      route(Method.Get, "/playlists", searchHandler.searchPlaylists)
  }

  def forUserTracksHandler(userTracksHandler: UserTracksHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/users/:userId/tracks", userTracksHandler.getUserTracks) :::
      route(Method.Get, "/me/tracks", userTracksHandler.getMeTracks) :::
      route(Method.Get, "/users/:userId/tracks/:trackId", userTracksHandler.getTrackByUser) :::
      route(Method.Get, "/me/tracks/:trackId", userTracksHandler.getTrackByMe)
  }

  def forUserPlaylistsHandler(userPlaylistsHandler: UserPlaylistsHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/users/:userId/playlists", userPlaylistsHandler.getUserPlaylists) :::
      route(Method.Get, "/me/playlists", userPlaylistsHandler.getMePlaylists) :::
      route(Method.Get, "/users/:userId/playlists/:id", userPlaylistsHandler.getUserPlaylist) :::
      route(Method.Get, "/me/playlists/:id", userPlaylistsHandler.getMePlaylist)
  }

  def forRepostsHandler(repostsHandler: RepostsHandler): List[(Method, String, Handler)] = {
    route(Method.Put, "/e1/me/track_reposts/:id", repostsHandler.createTracksRepost) :::
      route(Method.Delete, "/e1/me/track_reposts/:id", repostsHandler.deleteTracksRepost) :::
      route(Method.Put, "/e1/me/playlist_reposts/:id", repostsHandler.createPlaylistsRepost) :::
      route(Method.Delete, "/e1/me/playlist_reposts/:id", repostsHandler.deletePlaylistsRepost)
  }

  def forTimelineHandler(timelineHandler: TimelineHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/me/activities/tracks", timelineHandler.renderPublicTrackStream) :::
      route(Method.Get, "/me/activities/tracks/:tag", timelineHandler.renderPublicTrackStream) :::
      route(Method.Get, "/me/activities", timelineHandler.renderPublicStream) :::
      route(Method.Get, "/me/activities/all", timelineHandler.renderPublicStream) :::
      route(Method.Get, "/me/activities/all/own", timelineHandler.renderPublicStream) :::
      route(Method.Get, "/me/followings/tracks", timelineHandler.renderFollowingTracks)
  }

  def forTrackStreamsHandler(trackStreamsHandler: TrackStreamsHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest) :::
      route(Method.Get, "/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest) :::
      route(Method.Get, "/i1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest)
  }

  def forTrackDownloadHandler(trackDownloadHandler: TrackDownloadHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/download", trackDownloadHandler.handle)
  }

  def forLikesHandler(likesHandler: LikesHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/users/:userId/favorites/:trackId", likesHandler.getUserLikedTrackId) :::
      route(Method.Get, "/me/favorites/:trackId", likesHandler.getMeLikedTrackId) :::
      route(Method.Post, "/me/favorites/:trackId", likesHandler.createMeLikedTrackId) :::
      route(Method.Put, "/me/favorites/:trackId", likesHandler.createMeLikedTrackId) :::
      route(Method.Delete, "/me/favorites/:trackId", likesHandler.deleteMeLikedTrackId) :::
      route(Method.Get, "/users/:userId/favorites", likesHandler.getUserTracksLikes) :::
      route(Method.Get, "/me/favorites", likesHandler.getMeTracksLikes)
  }

  def forDummyHandler(): List[(Method, String, Handler)] = {
    route(Method.Get, "/dummy", (_) => Future.value(JsonResponseBuilder.ok("{}")))
  }
}
