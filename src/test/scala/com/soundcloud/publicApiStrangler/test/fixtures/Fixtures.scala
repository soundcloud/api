package com.soundcloud.publicApiStrangler.test.fixtures

import com.soundcloud.publicApiStrangler.test.UnitSpecification


trait Fixtures {
  this: UnitSpecification =>

  val singleTrack = withContentsOf("public_api", "single_track")
  val tracksArray = withContentsOf("public_api", "tracks_array")
  val playlist = withContentsOf("public_api", "playlist")
  val user = withContentsOf("public_api", "user")
  val users = withContentsOf("public_api", "users")
  val usersInCollection = withContentsOf("public_api", "users_in_collection")
  val objectsWithUsers = withContentsOf("public_api", "objects_with_users")
  val stream = withContentsOf("public_api", "stream")
  val streamFiltered = withContentsOf("public_api", "streamFiltered")
  val generic = withContentsOf("public_api", "generic")
  val genericFiltered = withContentsOf("public_api", "genericFiltered")

  val timelineStream = withContentsOf("timeline", "stream")
  val timelineActivities = withContentsOf("timeline", "activities")
  val timelineFollowingsTracks = withContentsOf("timeline", "followingsTracks")

  val lieblingLikesInfo = withContentsOf("liebling", "likes_info")

  val followsError = withContentsOf("follows", "follow_failed_normal")
  val followsAgeRestrictedError = withContentsOf("follows", "follow_failed_age_restricted")
  val followsAgeUnknownError = withContentsOf("follows", "follow_failed_age_unknown")

  val okidokiFetch = withContentsOf("okidoki", "fetch")
  val okidokiUsers = withContentsOf("okidoki", "users")

  val trackCoordinatorTrack = withContentsOf("track-coordinator", "track")
  val trackCoordinatorTrackInPublicApiFormat = withContentsOf("track-coordinator", "coordinator-track-in-public-api-format")

  val consumerSubscription = withContentsOf("subscriptions", "consumer-subscription")

  val trackmetadataClientEmptyTracks = withContentsOf("trackmetadataclient", "empty_tracks")
  val trackmetadataClientMultipleTracks = withContentsOf("trackmetadataclient", "multiple_tracks")
  val trackmetadataClientTracks_2 = withContentsOf("trackmetadataclient", "track2")
  val trackmetadataClientTracks_rogue = withContentsOf("trackmetadataclient", "track_rogue_attributes")
  val trackmetadataClientTracks_1_3 = withContentsOf("trackmetadataclient", "tracks_1_3")
  val trackmetadataClientNullableBooleans = withContentsOf("trackmetadataclient", "nullable_booleans")
}
