package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.http.server.Handler
import com.soundcloud.publicApiStrangler.controller._
import com.soundcloud.publicApiStrangler.support.DispatchToMothershipHandler
import com.twitter.finagle.http.Method

object RoutingDefinitions {

  def forUserFollowController(userFollowController: UserFollowController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/users/:id/followings", userFollowController.fetchFollowingsWithoutAuth),
      (Method.Get, "/users/:id/followings.json", userFollowController.fetchFollowingsWithoutAuth),
      (Method.Get, "/users/:id/followers", userFollowController.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers.json", userFollowController.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/recent", userFollowController.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/recent.json", userFollowController.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/ids", userFollowController.fetchFollowerIdsWithoutAuth),
      (Method.Get, "/users/:id/followers/ids.json", userFollowController.fetchFollowerIdsWithoutAuth),
      (Method.Get, "/users/:id/followings/ids", userFollowController.fetchFollowingIdsWithoutAuth),
      (Method.Get, "/users/:id/followings/ids.json", userFollowController.fetchFollowingIdsWithoutAuth),
      (Method.Get, "/users/:id/followers/followed_by/:other_id", userFollowController.fetchFollowersFollowed),
      (Method.Get, "/users/:id/followers/followed_by/:other_id.json", userFollowController.fetchFollowersFollowed),
      (Method.Get, "/users/:id/followings/not_followed_by/:other_id", userFollowController.fetchFollowingsNotFollowedBy),
      (Method.Get, "/users/:id/followings/not_followed_by/:other_id.json", userFollowController.fetchFollowingsNotFollowedBy),
      (Method.Get, "/users/:id/followings/common_to/:other_id", userFollowController.fetchMutualFollowings),
      (Method.Get, "/users/:id/followings/common_to/:other_id.json", userFollowController.fetchMutualFollowings),
      (Method.Get, "/users/:id/followers/:other_id", userFollowController.fetchPossibleFollowerWithoutAuth),
      (Method.Get, "/users/:id/followers/:other_id.json", userFollowController.fetchPossibleFollowerWithoutAuth),
      (Method.Get, "/users/:id/followings/:other_id", userFollowController.fetchPossibleFollowingWithoutAuth),
      (Method.Get, "/users/:id/followings/:other_id.json", userFollowController.fetchPossibleFollowingWithoutAuth),
      (Method.Get, "/me/followings", userFollowController.fetchFollowings),
      (Method.Get, "/me/followings.json", userFollowController.fetchFollowings),
      (Method.Get, "/me/followers", userFollowController.fetchMyFollowers),
      (Method.Get, "/me/followers.json", userFollowController.fetchMyFollowers),
      (Method.Get, "/me/followers/recent", userFollowController.fetchMyFollowers),
      (Method.Get, "/me/followers/recent.json", userFollowController.fetchMyFollowers),
      (Method.Get, "/me/followers/ids", userFollowController.fetchMyFollowerIds),
      (Method.Get, "/me/followers/ids.json", userFollowController.fetchMyFollowerIds),
      (Method.Get, "/me/followings/ids", userFollowController.fetchMyFollowingIds),
      (Method.Get, "/me/followings/ids.json", userFollowController.fetchMyFollowingIds),
      (Method.Get, "/me/followers/:other_id", userFollowController.fetchPossibleFollower),
      (Method.Get, "/me/followers/:other_id.json", userFollowController.fetchPossibleFollower),
      (Method.Get, "/me/followings/:other_id", userFollowController.fetchPossibleFollowing),
      (Method.Get, "/me/followings/:other_id.json", userFollowController.fetchPossibleFollowing),
      (Method.Post, "/me/followings/:other_id", userFollowController.follow),
      (Method.Post, "/me/followings/:other_id.json", userFollowController.follow),
      (Method.Put, "/me/followings/:other_id", userFollowController.follow),
      (Method.Put, "/me/followings/:other_id.json", userFollowController.follow),
      (Method.Delete, "/me/followings/:other_id", userFollowController.unfollow),
      (Method.Delete, "/me/followings/:other_id.json", userFollowController.unfollow),
      (Method.Get, "/v1/me/followings", userFollowController.fetchFollowings),
      (Method.Get, "/v1/me/followings.json", userFollowController.fetchFollowings),
      (Method.Get, "/v1/me/followers", userFollowController.fetchMyFollowers),
      (Method.Get, "/v1/me/followers.json", userFollowController.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/recent", userFollowController.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/recent.json", userFollowController.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/ids", userFollowController.fetchMyFollowerIds),
      (Method.Get, "/v1/me/followers/ids.json", userFollowController.fetchMyFollowerIds),
      (Method.Get, "/v1/me/followings/ids", userFollowController.fetchMyFollowingIds),
      (Method.Get, "/v1/me/followings/ids.json", userFollowController.fetchMyFollowingIds),
      (Method.Get, "/v1/me/followers/:other_id", userFollowController.fetchPossibleFollower),
      (Method.Get, "/v1/me/followers/:other_id.json", userFollowController.fetchPossibleFollower),
      (Method.Get, "/v1/me/followings/:other_id", userFollowController.fetchPossibleFollowing),
      (Method.Get, "/v1/me/followings/:other_id.json", userFollowController.fetchPossibleFollowing),
      (Method.Post, "/v1/me/followings/:other_id", userFollowController.follow),
      (Method.Post, "/v1/me/followings/:other_id.json", userFollowController.follow),
      (Method.Put, "/v1/me/followings/:other_id", userFollowController.follow),
      (Method.Put, "/v1/me/followings/:other_id.json", userFollowController.follow),
      (Method.Delete, "/v1/me/followings/:other_id", userFollowController.unfollow),
      (Method.Delete, "/v1/me/followings/:other_id.json", userFollowController.unfollow)
    )
  }

  def forMothershipDispatcher(mothershipDispatcher: DispatchToMothershipHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Post, "/playlists", mothershipDispatcher.dispatch),
      (Method.Put, "/playlists/:id", mothershipDispatcher.dispatch),
      (Method.Put, "/playlists/:id.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments.json/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download.json/", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks/:trackId", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks/:trackId.json", mothershipDispatcher.dispatch),
      (Method.Post, "/users/:userId/tracks", mothershipDispatcher.dispatch),
      (Method.Get, "/resolve", mothershipDispatcher.dispatch),
      (Method.Get, "/resolve.json", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token/", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token.json", mothershipDispatcher.dispatch),
      (Method.Get, "/announcements", mothershipDispatcher.dispatch),
      (Method.Get, "/announcements.json", mothershipDispatcher.dispatch),
      (Method.Head, "/me/followings/:other_id", mothershipDispatcher.dispatch),
      (Method.Head, "/me/followings/:other_id.json", mothershipDispatcher.dispatch),
      (Method.Head, "/v1/me/followings/:other_id", mothershipDispatcher.dispatch),
      (Method.Head, "/v1/me/followings/:other_id.json", mothershipDispatcher.dispatch)
    )
  }

  def forSingleTrackController(singleTrackController: SingleTrackController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId", singleTrackController.renderTrack),
      (Method.Get, "/tracks/:trackId/", singleTrackController.renderTrack),
      (Method.Get, "/tracks/:trackId.json", singleTrackController.renderTrack),
      (Method.Get, "/tracks/:trackId.json/", singleTrackController.renderTrack)
    )
  }

  def forPlaylistontroller(playlistsController: PlaylistsController): List[(Method, String, Handler)] = {
    List(
      (Method.Delete, "/playlists/:id", playlistsController.handleDelete),
      (Method.Delete, "/playlists/:id.json", playlistsController.handleDelete)
    )
  }

  def forSimilarSoundsController(similarSoundsController: SimilarSoundsController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId/related", similarSoundsController.handleSimilarSoundsRequest),
      (Method.Get, "/tracks/:trackId/related.json", similarSoundsController.handleSimilarSoundsRequest)
    )
  }

  def forTracksController(tracksController: TracksController): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/tracks/:trackId", tracksController.handlePut),
      (Method.Put, "/tracks/:trackId.json", tracksController.handlePut),
      (Method.Delete, "/tracks/:trackId", tracksController.handleDelete)
    )
  }

  def forUserRelatedMothershipDispatcher(userRelatedMothershipDispatcher: UserRelatedMothershipDispatcher): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/me/suggested/users/:category", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me/suggested/users/:category.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/suggested", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/suggested.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me/connections/friends", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me/connections/friends.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search/universal", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search/universal.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search/people", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/search/people.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/tracks/:id/favoriters", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/tracks/:id/favoriters.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/tracks/:id/favoriters/:user_id", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/tracks/:id/favoriters/:user_id.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id/", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id.json/", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id/comments", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id/comments/", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id/comments.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/users/:id/comments.json/", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me/", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me.json", userRelatedMothershipDispatcher.dispatchToMothership),
      (Method.Get, "/me.json/", userRelatedMothershipDispatcher.dispatchToMothership)
    )
  }

  def forSearchController(searchController: SearchController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks", searchController.dispatchTrackRequest),
      (Method.Get, "/tracks/", searchController.dispatchTrackRequest),
      (Method.Get, "/tracks.json", searchController.dispatchTrackRequest),
      (Method.Get, "/tracks.json/", searchController.dispatchTrackRequest),
      (Method.Get, "/v1/tracks", searchController.dispatchTrackRequest),
      (Method.Get, "/v1/tracks.json", searchController.dispatchTrackRequest),
      (Method.Get, "/users", searchController.dispatchUserRequest),
      (Method.Get, "/users/", searchController.dispatchUserRequest),
      (Method.Get, "/users.json", searchController.dispatchUserRequest),
      (Method.Get, "/playlists", searchController.dispatchPlaylistRequest),
      (Method.Get, "/playlists.json", searchController.dispatchPlaylistRequest)
    )
  }

  def forUserTracksController(userTracksController: UserTracksController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/users/:userId/tracks", userTracksController.handleRequest),
      (Method.Get, "/users/:userId/tracks/", userTracksController.handleRequest),
      (Method.Get, "/users/:userId/tracks.json", userTracksController.handleRequest),
      (Method.Get, "/users/:userId/tracks.json/", userTracksController.handleRequest)
    )
  }

  def forRepostsController(repostsController: RepostsController): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/e1/me/track_reposts/:id", repostsController.createTracksRepost),
      (Method.Put, "/e1/me/track_reposts/:id.json", repostsController.createTracksRepost),
      (Method.Delete, "/e1/me/track_reposts/:id", repostsController.deleteTracksRepost),
      (Method.Delete, "/e1/me/track_reposts/:id.json", repostsController.deleteTracksRepost),
      (Method.Put, "/e1/me/playlist_reposts/:id", repostsController.createPlaylistsRepost),
      (Method.Put, "/e1/me/playlist_reposts/:id.json", repostsController.createPlaylistsRepost),
      (Method.Delete, "/e1/me/playlist_reposts/:id", repostsController.deletePlaylistsRepost),
      (Method.Delete, "/e1/me/playlist_reposts/:id.json", repostsController.deletePlaylistsRepost),
      (Method.Get, "/e1/me/track_reposts/ids", repostsController.getUserRepostableTracks),
      (Method.Get, "/e1/me/track_reposts/ids.json", repostsController.getUserRepostableTracks),
      (Method.Get, "/e1/me/playlist_reposts/ids", repostsController.getUserRepostablePlaylists),
      (Method.Get, "/e1/me/playlist_reposts/ids.json", repostsController.getUserRepostablePlaylists)
    )
  }

  def forRepostersController(repostersController: RepostersController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/e1/tracks/:id/reposters", repostersController.trackReposters),
      (Method.Get, "/e1/tracks/:id/reposters.json", repostersController.trackReposters),
      (Method.Get, "/e1/playlists/:id/reposters", repostersController.playlistReposters),
      (Method.Get, "/e1/playlists/:id/reposters.json", repostersController.playlistReposters)
    )
  }

  def forSpamWarningsController(spamWarningsController: SpamWarningsController): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/me/spam_warnings/:warning_id/ack", spamWarningsController.handle)
    )
  }


  def forTimelineController(timelineController: TimelineController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/e1/me/activities", timelineController.renderAllActivities),
      (Method.Get, "/e1/me/activities.json", timelineController.renderAllActivities),
      (Method.Get, "/e1/me/stream", timelineController.renderStreamActivities),
      (Method.Get, "/e1/me/stream.json", timelineController.renderStreamActivities),
      (Method.Get, "/me/activities", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities.json", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/track", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/tracks", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/tracks.json", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/:tag", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/:tag.json", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/all", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/all.json", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/all/own", timelineController.renderPublicActivities),
      (Method.Get, "/me/activities/all/own.json", timelineController.renderPublicActivities),
      (Method.Get, "/me/followings/tracks", timelineController.renderFollowingsTracks),
      (Method.Get, "/me/followings/tracks.json", timelineController.renderFollowingsTracks)
    )
  }

  def forTrackStreamsController(trackStreamsController: TrackStreamsController): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams", trackStreamsController.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream/", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream/", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream/", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream/", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams/", trackStreamsController.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream.json", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream.json", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream.json", trackStreamsController.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream.json", trackStreamsController.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams.json", trackStreamsController.handleStreamRequest)
    )
  }

}
