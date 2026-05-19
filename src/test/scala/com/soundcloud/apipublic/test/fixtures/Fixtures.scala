package com.soundcloud.apipublic.test.fixtures

import com.soundcloud.apipublic.test.util.{GlobalJsonFiles, JsonFiles}
import play.api.libs.json.JsValue

object Fixtures {
  lazy val fixtureFiles: JsonFiles = GlobalJsonFiles

  def withContentsOf(prefix: String, name: String): JsValue = contentsOf(prefix, name)

  def fileJson(prefix: String, name: String) = contentsOf(prefix, name)

  def contentsOf(prefix: String, name: String): JsValue = fixtureFiles.load(s"$prefix/$name") match {
    case Some(contents) => contents
    case None => throw new IllegalStateException(s"File [$prefix/$name] not found")
  }

  lazy val timelineMapperStream = contentsOf("timelinemapper", "stream")
  lazy val timelineMapperActivities = contentsOf("timelinemapper", "activities")
  lazy val timelineFollowingsTracks = withContentsOf("timeline", "followingsTracks")

  lazy val entityMapperOkidokiFetch = contentsOf("entitymapper", "fetch")

  lazy val followsError = contentsOf("follows", "follow_failed_normal")
  lazy val followsAgeRestrictedError = contentsOf("follows", "follow_failed_age_restricted")
  lazy val followsAgeUnknownError = contentsOf("follows", "follow_failed_age_unknown")

  lazy val trackCoordinatorTrack = contentsOf("track-coordinator", "track")
  lazy val trackCoordinatorTrackNoCounts = contentsOf("track-coordinator", "trackNoCounts")
  lazy val trackCoordinatorUploadQuota = contentsOf("track-coordinator", "upload-quota")

  lazy val consumerSubscription = contentsOf("subscriptions", "consumer-subscription")
  lazy val submarineCreatorSubscription = contentsOf("subscriptions", "submarine/creator-subscription")

  lazy val timelineItemStream = contentsOf("timeline", "item_stream")
  lazy val timelineStream = contentsOf("timeline", "stream")
  lazy val timelineProfile = contentsOf("timeline", "profile")
  lazy val timelinePostedAndRepostedTracks = contentsOf("timeline", "posted_and_reposted_tracks")
  lazy val timelinePostedAndRepostedPlaylists = contentsOf("timeline", "posted_and_reposted_playlists")
  lazy val timelinePostedAndLikedPlaylists = contentsOf("timeline", "posted_and_liked_playlists")
  lazy val timelineReposts = contentsOf("timeline", "reposts")
  lazy val timelineLikes = contentsOf("timeline", "likes")

  lazy val okidokiUsers = contentsOf("okidoki", "users")
  lazy val okidokiUsersWithDeprecatedCounts = contentsOf("okidoki", "users_with_deprecated_counts")
  lazy val okidokiSpamWarning = contentsOf("okidoki", "spam_warning")
  lazy val trackmetadataClientTracks_chrono = contentsOf("trackmetadataclient", "tracks_chrono")

  lazy val moshiUser = contentsOf("moshimoshi", "user")
  lazy val moshiUsers = contentsOf("moshimoshi", "users")
  lazy val moshiUser2 = contentsOf("moshimoshi", "user2")
  lazy val moshimoshiPlaylistsChrono = contentsOf("moshimoshi", "playlists_chrono")
  lazy val webProfiles = contentsOf("moshimoshi", "web-profiles")

  lazy val similarSoundsNonEmpty = contentsOf("similar-sounds", "non-empty")
  lazy val similarCreatorsNonEmpty = contentsOf("similar-creators", "non-empty")

  lazy val updateTrackJson = fileJson("tracks", "update_track_json")
  lazy val createTrackJson = fileJson("tracks", "create_track")
}
