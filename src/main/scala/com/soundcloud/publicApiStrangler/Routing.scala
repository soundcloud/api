package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.http.server.Handler
import com.soundcloud.publicApiStrangler.handler.{DispatchToMothershipHandler, _}
import com.twitter.finagle.http.Method

object Routing {

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
      route(Method.Get, "/users/:id/followers/recent", userFollowHandler.fetchFollowersWithoutAuth) :::
      route(Method.Get, "/users/:id/followers/ids", userFollowHandler.fetchFollowerIdsWithoutAuth) :::
      route(Method.Get, "/users/:id/followings/ids", userFollowHandler.fetchFollowingIdsWithoutAuth) :::
      route(Method.Get, "/users/:id/followers/followed_by/:other_id", userFollowHandler.fetchFollowersFollowed) :::
      route(
        Method.Get,
        "/users/:id/followings/not_followed_by/:other_id",
        userFollowHandler.fetchFollowingsNotFollowedBy
      ) :::
      route(
        Method.Get,
        "/users/:id/followings/common_to/:other_id",
        userFollowHandler.fetchMutualFollowings
      ) :::
      route(Method.Get, "/users/:id/followers/:other_id", userFollowHandler.fetchPossibleFollowerWithoutAuth) :::
      route(Method.Get, "/users/:id/followings/:other_id", userFollowHandler.fetchPossibleFollowingWithoutAuth) :::
      route(Method.Get, "/me/followings", userFollowHandler.fetchFollowings) :::
      route(Method.Get, "/me/followers", userFollowHandler.fetchMyFollowers) :::
      route(Method.Get, "/me/followers/recent", userFollowHandler.fetchMyFollowers) :::
      route(Method.Get, "/me/followers/ids", userFollowHandler.fetchMyFollowerIds) :::
      route(Method.Get, "/me/followings/ids", userFollowHandler.fetchMyFollowingIds) :::
      route(Method.Get, "/me/followers/:other_id", userFollowHandler.fetchPossibleFollower) :::
      route(Method.Get, "/me/followings/:other_id", userFollowHandler.fetchPossibleFollowing) :::
      route(Method.Post, "/me/followings/:other_id", userFollowHandler.follow) :::
      route(Method.Put, "/me/followings/:other_id", userFollowHandler.follow) :::
      route(Method.Delete, "/me/followings/:other_id", userFollowHandler.unfollow)
  }

  def forMothershipDispatcher(mothershipDispatcher: DispatchToMothershipHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/apps", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/apps/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/announcements", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/comments", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/comments", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/connect", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/connections", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/connections/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/likes", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/e1/me/playlist_likes", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/playlist_likes", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/e1/me/playlist_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/e1/me/playlist_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/playlist_likes/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/sounds", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/sounds/mini", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/sounds/ids", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/e1/me/track_likes", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/track_likes", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/e1/me/track_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/e1/me/track_likes/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/me/track_likes/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/likes", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/playlist_likes", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/sounds", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/stream", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/track_likes", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/e1/users/:userId/track_likes/ids", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/i1/comments/:comment_id/spam", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/i1/me/shortcuts", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/me", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me/blockings", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/comments", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me/comments", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/me/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/connections", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/connections/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/email", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/mutings/users/ids", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/playlists/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/playlists/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/playlists/:playlistId/tracks", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/resolve", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/resolve", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/tracks", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/tracks/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/tracks/:trackId/comments", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/tracks/:trackId/comments", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/tracks/:trackId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/tracks/:trackId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/tracks/:trackId/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/tracks/:trackId/plays", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/transcodings", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/transcodings/:uid", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/upload/policy", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/users", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/users/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/users/:id", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/users/:userId/comments", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/users/:userId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/comments/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/connections", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/connections/:id", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/users/:userId/tracks", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/tracks/:id", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/users/:userId/tracks/:id", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/users/:userId/tracks/:id", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/tracks/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/favorites", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/favorites/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/me/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/favorites", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/favorites/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/users/:userId/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/users/:userId/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/users/:userId/favorites/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/tracks", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/tracks/ids", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/tracks/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me/tracks/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/me/tracks/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/me/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/me/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/me/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Post, "/users/:userId/playlists", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Put, "/users/:userId/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Delete, "/users/:userId/playlists/:trackId", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/me/web-profiles", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/users/:userId/web-profiles", mothershipDispatcher.dispatch) :::
      route(Method.Get, "/oembed", mothershipDispatcher.dispatch)
  }

  def forTokenExchange(handler: Handler): List[(Method, String, Handler)] = {
    route(Method.Post, "/oauth2/token", handler)
  }

  def forSingleTrackHandler(singleTrackHandler: SingleTrackHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId", singleTrackHandler.renderTrack)
  }

  def forPlaylistHandler(playlistsHandler: PlaylistsHandler): List[(Method, String, Handler)] = {
    route(Method.Delete, "/playlists/:id", playlistsHandler.handleDelete)
  }

  def forSimilarSoundsHandler(similarSoundsHandler: SimilarSoundsHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/related", similarSoundsHandler.handleSimilarSoundsRequest)
  }

  def forTracksHandler(tracksHandler: TracksHandler): List[(Method, String, Handler)] = {
    route(Method.Put, "/tracks/:trackId", tracksHandler.handlePut) :::
      route(Method.Delete, "/tracks/:trackId", tracksHandler.handleDelete)
  }

  def forUserRelatedMothershipDispatcher(
      userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher
  ): List[(Method, String, Handler)] = {
    route(Method.Get, "/me/suggested/users/:category", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/users/suggested", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/me/connections/friends", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/tracks/:id/favoriters", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/tracks/:id/favoriters/:user_id", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/users/:id", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/users/:id/comments", userRelatedMothershipDispatcher.dispatchToMothership) :::
      route(Method.Get, "/me", userRelatedMothershipDispatcher.dispatchToMothership)
  }

  def forSearchHandler(searchHandler: SearchHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks", searchHandler.dispatchTrackRequest) :::
      route(Method.Get, "/users", searchHandler.dispatchUserRequest) :::
      route(Method.Get, "/playlists", searchHandler.dispatchPlaylistRequest)
  }

  def forUserTracksHandler(userTracksHandler: UserTracksHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/users/:userId/tracks", userTracksHandler.handleRequest)
  }

  def forRepostsHandler(repostsHandler: RepostsHandler): List[(Method, String, Handler)] = {
    route(Method.Put, "/e1/me/track_reposts/:id", repostsHandler.createTracksRepost) :::
      route(Method.Delete, "/e1/me/track_reposts/:id", repostsHandler.deleteTracksRepost) :::
      route(Method.Put, "/e1/me/playlist_reposts/:id", repostsHandler.createPlaylistsRepost) :::
      route(Method.Delete, "/e1/me/playlist_reposts/:id", repostsHandler.deletePlaylistsRepost) :::
      route(Method.Get, "/e1/me/track_reposts/ids", repostsHandler.getUserRepostableTracks) :::
      route(Method.Get, "/e1/me/playlist_reposts/ids", repostsHandler.getUserRepostablePlaylists)
  }

  def forRepostersHandler(repostersHandler: RepostersHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/e1/tracks/:id/reposters", repostersHandler.trackReposters) :::
      route(Method.Get, "/e1/playlists/:id/reposters", repostersHandler.playlistReposters)
  }

  def forTimelineHandler(timelineHandler: TimelineHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/e1/me/activities", timelineHandler.renderAllActivities) :::
      route(Method.Get, "/e1/me/stream", timelineHandler.renderStreamActivities) :::
      route(Method.Get, "/me/activities", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/activities/track", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/activities/tracks", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/activities/tracks/:tag", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/activities/all", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/activities/all/own", timelineHandler.renderPublicActivities) :::
      route(Method.Get, "/me/followings/tracks", timelineHandler.renderFollowingsTracks)
  }

  def forTrackStreamsHandler(trackStreamsHandler: TrackStreamsHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest) :::
      route(Method.Get, "/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest) :::
      route(Method.Get, "/i1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest)
  }

  def forTrackDownloadHandler(trackDownloadHandler: TrackDownloadHandler): List[(Method, String, Handler)] = {
    route(Method.Get, "/tracks/:trackId/download", trackDownloadHandler.handle)
  }
}
