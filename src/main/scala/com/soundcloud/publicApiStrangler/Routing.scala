package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.http.server.Handler
import com.soundcloud.publicApiStrangler.handler.{DispatchToMothershipHandler, _}
import com.twitter.finagle.http.Method

object Routing {

  def forUserFollowHandler(userFollowHandler: UserFollowHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/users/:id/followings", userFollowHandler.fetchFollowingsWithoutAuth),
      (Method.Get, "/users/:id/followings.json", userFollowHandler.fetchFollowingsWithoutAuth),
      (Method.Get, "/users/:id/followers", userFollowHandler.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers.json", userFollowHandler.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/recent", userFollowHandler.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/recent.json", userFollowHandler.fetchFollowersWithoutAuth),
      (Method.Get, "/users/:id/followers/ids", userFollowHandler.fetchFollowerIdsWithoutAuth),
      (Method.Get, "/users/:id/followers/ids.json", userFollowHandler.fetchFollowerIdsWithoutAuth),
      (Method.Get, "/users/:id/followings/ids", userFollowHandler.fetchFollowingIdsWithoutAuth),
      (Method.Get, "/users/:id/followings/ids.json", userFollowHandler.fetchFollowingIdsWithoutAuth),
      (Method.Get, "/users/:id/followers/followed_by/:other_id", userFollowHandler.fetchFollowersFollowed),
      (Method.Get, "/users/:id/followers/followed_by/:other_id.json", userFollowHandler.fetchFollowersFollowed),
      (Method.Get, "/users/:id/followings/not_followed_by/:other_id", userFollowHandler.fetchFollowingsNotFollowedBy),
      (Method.Get, "/users/:id/followings/not_followed_by/:other_id.json", userFollowHandler.fetchFollowingsNotFollowedBy),
      (Method.Get, "/users/:id/followings/common_to/:other_id", userFollowHandler.fetchMutualFollowings),
      (Method.Get, "/users/:id/followings/common_to/:other_id.json", userFollowHandler.fetchMutualFollowings),
      (Method.Get, "/users/:id/followers/:other_id", userFollowHandler.fetchPossibleFollowerWithoutAuth),
      (Method.Get, "/users/:id/followers/:other_id.json", userFollowHandler.fetchPossibleFollowerWithoutAuth),
      (Method.Get, "/users/:id/followings/:other_id", userFollowHandler.fetchPossibleFollowingWithoutAuth),
      (Method.Get, "/users/:id/followings/:other_id.json", userFollowHandler.fetchPossibleFollowingWithoutAuth),
      (Method.Get, "/me/followings", userFollowHandler.fetchFollowings),
      (Method.Get, "/me/followings.json", userFollowHandler.fetchFollowings),
      (Method.Get, "/me/followers", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/me/followers.json", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/me/followers/recent", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/me/followers/recent.json", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/me/followers/ids", userFollowHandler.fetchMyFollowerIds),
      (Method.Get, "/me/followers/ids.json", userFollowHandler.fetchMyFollowerIds),
      (Method.Get, "/me/followings/ids", userFollowHandler.fetchMyFollowingIds),
      (Method.Get, "/me/followings/ids.json", userFollowHandler.fetchMyFollowingIds),
      (Method.Get, "/me/followers/:other_id", userFollowHandler.fetchPossibleFollower),
      (Method.Get, "/me/followers/:other_id.json", userFollowHandler.fetchPossibleFollower),
      (Method.Get, "/me/followings/:other_id", userFollowHandler.fetchPossibleFollowing),
      (Method.Get, "/me/followings/:other_id.json", userFollowHandler.fetchPossibleFollowing),
      (Method.Post, "/me/followings/:other_id", userFollowHandler.follow),
      (Method.Post, "/me/followings/:other_id.json", userFollowHandler.follow),
      (Method.Put, "/me/followings/:other_id", userFollowHandler.follow),
      (Method.Put, "/me/followings/:other_id.json", userFollowHandler.follow),
      (Method.Delete, "/me/followings/:other_id", userFollowHandler.unfollow),
      (Method.Delete, "/me/followings/:other_id.json", userFollowHandler.unfollow),
      (Method.Get, "/v1/me/followings", userFollowHandler.fetchFollowings),
      (Method.Get, "/v1/me/followings.json", userFollowHandler.fetchFollowings),
      (Method.Get, "/v1/me/followers", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/v1/me/followers.json", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/recent", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/recent.json", userFollowHandler.fetchMyFollowers),
      (Method.Get, "/v1/me/followers/ids", userFollowHandler.fetchMyFollowerIds),
      (Method.Get, "/v1/me/followers/ids.json", userFollowHandler.fetchMyFollowerIds),
      (Method.Get, "/v1/me/followings/ids", userFollowHandler.fetchMyFollowingIds),
      (Method.Get, "/v1/me/followings/ids.json", userFollowHandler.fetchMyFollowingIds),
      (Method.Get, "/v1/me/followers/:other_id", userFollowHandler.fetchPossibleFollower),
      (Method.Get, "/v1/me/followers/:other_id.json", userFollowHandler.fetchPossibleFollower),
      (Method.Get, "/v1/me/followings/:other_id", userFollowHandler.fetchPossibleFollowing),
      (Method.Get, "/v1/me/followings/:other_id.json", userFollowHandler.fetchPossibleFollowing),
      (Method.Post, "/v1/me/followings/:other_id", userFollowHandler.follow),
      (Method.Post, "/v1/me/followings/:other_id.json", userFollowHandler.follow),
      (Method.Put, "/v1/me/followings/:other_id", userFollowHandler.follow),
      (Method.Put, "/v1/me/followings/:other_id.json", userFollowHandler.follow),
      (Method.Delete, "/v1/me/followings/:other_id", userFollowHandler.unfollow),
      (Method.Delete, "/v1/me/followings/:other_id.json", userFollowHandler.unfollow)
    )
  }

  def forMothershipDispatcher(mothershipDispatcher: DispatchToMothershipHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/announcements", mothershipDispatcher.dispatch),
      (Method.Get, "/announcements.json", mothershipDispatcher.dispatch),
      (Method.Head, "/me/followings/:other_id", mothershipDispatcher.dispatch),
      (Method.Head, "/me/followings/:other_id.json", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token.json", mothershipDispatcher.dispatch),
      (Method.Post, "/oauth2/token/", mothershipDispatcher.dispatch),
      (Method.Post, "/playlists", mothershipDispatcher.dispatch),
      (Method.Put, "/playlists/:id", mothershipDispatcher.dispatch),
      (Method.Put, "/playlists/:id.json", mothershipDispatcher.dispatch),
      (Method.Get, "/resolve", mothershipDispatcher.dispatch),
      (Method.Get, "/resolve.json", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks.json", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks.json/", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks/", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks/:trackId", mothershipDispatcher.dispatch),
      (Method.Post, "/tracks/:trackId.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments.json/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/comments/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download.json", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download.json/", mothershipDispatcher.dispatch),
      (Method.Get, "/tracks/:trackId/download/", mothershipDispatcher.dispatch),
      (Method.Post, "/users/:userId/tracks", mothershipDispatcher.dispatch),
      (Method.Head, "/v1/me/followings/:other_id", mothershipDispatcher.dispatch),
      (Method.Head, "/v1/me/followings/:other_id.json", mothershipDispatcher.dispatch)
    )
  }

  def forSingleTrackHandler(singleTrackHandler: SingleTrackHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId", singleTrackHandler.renderTrack),
      (Method.Get, "/tracks/:trackId/", singleTrackHandler.renderTrack),
      (Method.Get, "/tracks/:trackId.json", singleTrackHandler.renderTrack),
      (Method.Get, "/tracks/:trackId.json/", singleTrackHandler.renderTrack)
    )
  }

  def forPlaylistHandler(playlistsHandler: PlaylistsHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Delete, "/playlists/:id", playlistsHandler.handleDelete),
      (Method.Delete, "/playlists/:id.json", playlistsHandler.handleDelete)
    )
  }

  def forSimilarSoundsHandler(similarSoundsHandler: SimilarSoundsHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId/related", similarSoundsHandler.handleSimilarSoundsRequest),
      (Method.Get, "/tracks/:trackId/related.json", similarSoundsHandler.handleSimilarSoundsRequest)
    )
  }

  def forTracksHandler(tracksHandler: TracksHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/tracks/:trackId", tracksHandler.handlePut),
      (Method.Put, "/tracks/:trackId.json", tracksHandler.handlePut),
      (Method.Delete, "/tracks/:trackId", tracksHandler.handleDelete)
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

  def forSearchHandler(searchHandler: SearchHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks", searchHandler.dispatchTrackRequest),
      (Method.Get, "/tracks/", searchHandler.dispatchTrackRequest),
      (Method.Get, "/tracks.json", searchHandler.dispatchTrackRequest),
      (Method.Get, "/tracks.json/", searchHandler.dispatchTrackRequest),
      (Method.Get, "/v1/tracks", searchHandler.dispatchTrackRequest),
      (Method.Get, "/v1/tracks.json", searchHandler.dispatchTrackRequest),
      (Method.Get, "/users", searchHandler.dispatchUserRequest),
      (Method.Get, "/users/", searchHandler.dispatchUserRequest),
      (Method.Get, "/users.json", searchHandler.dispatchUserRequest),
      (Method.Get, "/playlists", searchHandler.dispatchPlaylistRequest),
      (Method.Get, "/playlists.json", searchHandler.dispatchPlaylistRequest)
    )
  }

  def forUserTracksHandler(userTracksHandler: UserTracksHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/users/:userId/tracks", userTracksHandler.handleRequest),
      (Method.Get, "/users/:userId/tracks/", userTracksHandler.handleRequest),
      (Method.Get, "/users/:userId/tracks.json", userTracksHandler.handleRequest),
      (Method.Get, "/users/:userId/tracks.json/", userTracksHandler.handleRequest)
    )
  }

  def forRepostsHandler(repostsHandler: RepostsHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/e1/me/track_reposts/:id", repostsHandler.createTracksRepost),
      (Method.Put, "/e1/me/track_reposts/:id.json", repostsHandler.createTracksRepost),
      (Method.Delete, "/e1/me/track_reposts/:id", repostsHandler.deleteTracksRepost),
      (Method.Delete, "/e1/me/track_reposts/:id.json", repostsHandler.deleteTracksRepost),
      (Method.Put, "/e1/me/playlist_reposts/:id", repostsHandler.createPlaylistsRepost),
      (Method.Put, "/e1/me/playlist_reposts/:id.json", repostsHandler.createPlaylistsRepost),
      (Method.Delete, "/e1/me/playlist_reposts/:id", repostsHandler.deletePlaylistsRepost),
      (Method.Delete, "/e1/me/playlist_reposts/:id.json", repostsHandler.deletePlaylistsRepost),
      (Method.Get, "/e1/me/track_reposts/ids", repostsHandler.getUserRepostableTracks),
      (Method.Get, "/e1/me/track_reposts/ids.json", repostsHandler.getUserRepostableTracks),
      (Method.Get, "/e1/me/playlist_reposts/ids", repostsHandler.getUserRepostablePlaylists),
      (Method.Get, "/e1/me/playlist_reposts/ids.json", repostsHandler.getUserRepostablePlaylists)
    )
  }

  def forRepostersHandler(repostersHandler: RepostersHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/e1/tracks/:id/reposters", repostersHandler.trackReposters),
      (Method.Get, "/e1/tracks/:id/reposters.json", repostersHandler.trackReposters),
      (Method.Get, "/e1/playlists/:id/reposters", repostersHandler.playlistReposters),
      (Method.Get, "/e1/playlists/:id/reposters.json", repostersHandler.playlistReposters)
    )
  }

  def forSpamWarningsHandler(spamWarningsHandler: SpamWarningsHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Put, "/me/spam_warnings/:warning_id/ack", spamWarningsHandler.handle)
    )
  }


  def forTimelineHandler(timelineHandler: TimelineHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/e1/me/activities", timelineHandler.renderAllActivities),
      (Method.Get, "/e1/me/activities.json", timelineHandler.renderAllActivities),
      (Method.Get, "/e1/me/stream", timelineHandler.renderStreamActivities),
      (Method.Get, "/e1/me/stream.json", timelineHandler.renderStreamActivities),
      (Method.Get, "/me/activities", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities.json", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/track", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/tracks", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/tracks.json", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/:tag", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/tracks/:tag.json", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/all", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/all.json", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/all/own", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/activities/all/own.json", timelineHandler.renderPublicActivities),
      (Method.Get, "/me/followings/tracks", timelineHandler.renderFollowingsTracks),
      (Method.Get, "/me/followings/tracks.json", timelineHandler.renderFollowingsTracks)
    )
  }

  def forTrackStreamsHandler(trackStreamsHandler: TrackStreamsHandler): List[(Method, String, Handler)] = {
    List(
      (Method.Get, "/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream/", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream/", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream/", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream/", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams/", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/tracks/:trackId/stream.json", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/tracks/:trackId/stream.json", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest),
      (Method.Get, "/v1/tracks/:trackId/stream.json", trackStreamsHandler.redirectStreamRequest),
      (Method.Head, "/v1/tracks/:trackId/stream.json", trackStreamsHandler.redirectStreamRequest),
      (Method.Get, "/i1/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest),
      (Method.Head, "/i1/tracks/:trackId/streams.json", trackStreamsHandler.handleStreamRequest)
    )
  }

}
