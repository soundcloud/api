package com.soundcloud.publicApiStrangler.test.fixtures

import com.soundcloud.publicApiStrangler.test.util.{GlobalJsonFiles, JsonFiles}
import play.api.libs.json.JsValue

object Fixtures {
  lazy val fixtureFiles: JsonFiles = GlobalJsonFiles

  def withContentsOf(prefix: String, name: String): JsValue = contentsOf(prefix, name)

  def fileJson(prefix: String, name: String) = contentsOf(prefix, name)

  def contentsOf(prefix: String, name: String): JsValue = fixtureFiles.load(s"$prefix/$name") match {
    case Some(contents) => contents
    case None => throw new IllegalStateException(s"File [$prefix/$name] not found")
  }

  private def fileToString(prefix: String, name: String): String = {
    contentsOf(prefix, name).toString()
  }

  lazy val singleTrack = contentsOf("public_api", "single_track")
  lazy val tracksArray = contentsOf("public_api", "tracks_array")
  lazy val playlist = contentsOf("public_api", "playlist")
  lazy val user = contentsOf("public_api", "user")
  lazy val users = contentsOf("public_api", "users")
  lazy val usersInCollection = contentsOf("public_api", "users_in_collection")
  lazy val objectsWithUsers = contentsOf("public_api", "objects_with_users")
  lazy val stream = contentsOf("public_api", "stream")
  lazy val streamFiltered = contentsOf("public_api", "streamFiltered")
  lazy val generic = contentsOf("public_api", "generic")
  lazy val genericFiltered = contentsOf("public_api", "genericFiltered")

  lazy val timelineMapperStream = contentsOf("timelinemapper", "stream")
  lazy val timelineMapperActivities = contentsOf("timelinemapper", "activities")
  lazy val timelineFollowingsTracks = withContentsOf("timeline", "followingsTracks")

  lazy val entityMapperLieblingLikesInfo = contentsOf("entitymapper", "likes_info")
  lazy val entityMapperOkidokiFetch = contentsOf("entitymapper", "fetch")

  lazy val followsError = contentsOf("follows", "follow_failed_normal")
  lazy val followsAgeRestrictedError = contentsOf("follows", "follow_failed_age_restricted")
  lazy val followsAgeUnknownError = contentsOf("follows", "follow_failed_age_unknown")

  lazy val trackCoordinatorTrack = contentsOf("track-coordinator", "track")
  lazy val trackCoordinatorTrackInPublicApiFormat =
    contentsOf("track-coordinator", "coordinator-track-in-public-api-format")

  lazy val consumerSubscription = contentsOf("subscriptions", "consumer-subscription")

  lazy val timelineItemStream = contentsOf("timeline", "item_stream")
  lazy val timelineStream = contentsOf("timeline", "stream")
  lazy val timelineProfile = contentsOf("timeline", "profile")
  lazy val timelinePostedAndRepostedTracks = contentsOf("timeline", "posted_and_reposted_tracks")
  lazy val timelinePostedAndRepostedPlaylists = contentsOf("timeline", "posted_and_reposted_playlists")
  lazy val timelinePostedAndLikedPlaylists = contentsOf("timeline", "posted_and_liked_playlists")
  lazy val timelineReposts = contentsOf("timeline", "reposts")
  lazy val timelineLikes = contentsOf("timeline", "likes")

  lazy val okidokiUsers = contentsOf("okidoki", "users")
  lazy val trackmetadataClientTracks_chrono = contentsOf("trackmetadataclient", "tracks_chrono")

  lazy val moshiUser = contentsOf("moshimoshi", "user")
  lazy val moshiUsers = contentsOf("moshimoshi", "users")
  lazy val commentsUsers = contentsOf("public_api", "comment_users")
  lazy val moshiUser2 = contentsOf("moshimoshi", "user2")
  lazy val moshimoshiPlaylistsChrono = contentsOf("moshimoshi", "playlists_chrono")
  lazy val moshiComments = contentsOf("moshimoshi", "comments")

  lazy val similarSoundsNonEmpty = contentsOf("similar-sounds", "non-empty")

  lazy val tracksStreamResponse = fileJson("tracks", "stream_response")
  lazy val tracksDownloadResponse = fileJson("tracks", "download_response")
  lazy val updateTrackJson = fileJson("tracks", "update_track_json")

  lazy val lieblingLikeCreationSuccess = fileToString("liebling", "like_creation_success")
  lazy val lieblingLikeDeletionSuccess = fileToString("liebling", "like_deletion_success")
}
