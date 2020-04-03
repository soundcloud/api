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
  lazy val timelineActivities = contentsOf("timeline", "activities")
  lazy val timelineProfile = contentsOf("timeline", "profile")
  lazy val timelinePostedAndRepostedTracks = contentsOf("timeline", "posted_and_reposted_tracks")
  lazy val timelinePostedAndRepostedPlaylists = contentsOf("timeline", "posted_and_reposted_playlists")
  lazy val timelinePostedAndLikedPlaylists = contentsOf("timeline", "posted_and_liked_playlists")
  lazy val timelineReposts = contentsOf("timeline", "reposts")
  lazy val timelineLikes = contentsOf("timeline", "likes")

  lazy val trackmetadataClientEmptyTracks = contentsOf("trackmetadataclient", "empty_tracks")
  lazy val trackmetadataClientMultipleTracks = contentsOf("trackmetadataclient", "multiple_tracks")
  lazy val trackmetadataClientTracks_2 = contentsOf("trackmetadataclient", "track2")
  lazy val trackmetadataClientTracks_rogue = contentsOf("trackmetadataclient", "track_rogue_attributes")
  lazy val trackmetadataClientTracks_1_3 = contentsOf("trackmetadataclient", "tracks_1_3")
  lazy val trackmetadataClientNullableBooleans = contentsOf("trackmetadataclient", "nullable_booleans")

  lazy val okidokiThings = contentsOf("okidoki", "fetch")
  lazy val okidokiTrackPurchaseLinks = contentsOf("okidoki", "track_purchase_links")
  lazy val okidokiUsers = contentsOf("okidoki", "users")
  lazy val okidokiUserEmails = contentsOf("okidoki", "user_emails")
  lazy val okidokiUserEmail = contentsOf("okidoki", "user_email")
  lazy val okidokiRestrictionNone = contentsOf("okidoki", "fetch_restriction_none")
  lazy val okidokiRestrictionBlock = contentsOf("okidoki", "fetch_restriction_block")
  lazy val okidokiSpotlightWithItems = contentsOf("okidoki", "spotlight")
  lazy val okidokiSpotlightEmpty = contentsOf("okidoki", "spotlight_empty")
  lazy val okidokiSpotlightItem = contentsOf("okidoki", "spotlight_item")

  lazy val moshiErrors = contentsOf("moshimoshi", "errors")

  lazy val moshiFeatureActive = fileJson("moshimoshi", "feature_active")

  lazy val moshiPlaylists = contentsOf("moshimoshi", "playlists")
  lazy val moshiPlaylist = contentsOf("moshimoshi", "playlist")
  lazy val moshiPlaylist2 = contentsOf("moshimoshi", "playlist2")
  lazy val moshiPlaylistTracks = contentsOf("moshimoshi", "playlist_tracks")
  lazy val moshiPlaylistTracksWithPagination = contentsOf("moshimoshi", "playlist_tracks_with_pagination")
  lazy val moshiPlaylistTrackUrns = contentsOf("moshimoshi", "playlist_track_urns")
  lazy val moshiPlaylistLongDuration = contentsOf("moshimoshi", "playlist_long_duration")

  lazy val moshiTrackCreate = contentsOf("moshimoshi", "track_create")
  lazy val moshiTrackCreateWithArtwork = contentsOf("moshimoshi", "track_create_with_s3_artwork")
  lazy val moshiTrackCreateWithNullArtwork = contentsOf("moshimoshi", "track_create_with_null_s3_artwork")
  lazy val moshiTrackCreateWithoutPermalink = contentsOf("moshimoshi", "track_create_without_permalink")
  lazy val moshiTracks = contentsOf("moshimoshi", "tracks")
  lazy val moshiTrackGeoblockings = contentsOf("moshimoshi", "track_geoblocking")
  lazy val moshiTrackGeoblockingsNull = fileJson("moshimoshi", "track_geoblocking_null")
  lazy val moshiTrackMinimal = contentsOf("moshimoshi", "track_minimal")
  lazy val moshiTrackFull = contentsOf("moshimoshi", "track_full")
  lazy val moshiTrackFullWithNoDownloadsLeft = contentsOf("moshimoshi", "track_full_with_no_downloads_left")
  lazy val moshiTrackFullWithoutDownloadsLeft = contentsOf("moshimoshi", "track_full_without_downloads_left")
  lazy val moshiTrackUpdate = contentsOf("moshimoshi", "track_update")
  lazy val moshiTrackUpdateWithArtwork = fileJson("moshimoshi", "track_update_with_s3_artwork")
  lazy val moshiTrackUpdateWithNullArtwork = contentsOf("moshimoshi", "track_update_with_null_s3_artwork")
  lazy val moshiTrackUpdateWithPublishedAt = contentsOf("moshimoshi", "track_update_with_published_at")

  lazy val moshiUser = contentsOf("moshimoshi", "user")
  lazy val moshiUsers = contentsOf("moshimoshi", "users")
  lazy val moshiUser2 = contentsOf("moshimoshi", "user2")

  lazy val moshiWebProfiles = contentsOf("moshimoshi", "web_profiles")

  lazy val similarSoundsNonEmpty = contentsOf("similar-sounds", "non-empty")

  lazy val tracksStreamResponse = fileJson("tracks", "stream_response")
  lazy val tracksDownloadResponse = fileJson("tracks", "download_response")
  lazy val visibleTracksResponse = fileJson("tracks", "visible_tracks_response")
}
