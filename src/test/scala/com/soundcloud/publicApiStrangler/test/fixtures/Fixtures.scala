package com.soundcloud.publicApiStrangler.test.fixtures

import com.soundcloud.bff.test.UnitSpecification

trait Fixtures {
  this: UnitSpecification =>

  val singleTrack = withContentsOf("public_api", "single_track")
  val tracksArray = withContentsOf("public_api", "tracks_array")
  val playlist = withContentsOf("public_api", "playlist")
  val user = withContentsOf("public_api", "user")
  val stream = withContentsOf("public_api", "stream")
  val streamFiltered = withContentsOf("public_api", "streamFiltered")
  val generic = withContentsOf("public_api", "generic")
  val genericFiltered = withContentsOf("public_api", "genericFiltered")

  val timelineStream = withContentsOf("timeline", "stream")
  val timelineActivities = withContentsOf("timeline", "activities")

  val lieblingLikesInfo = withContentsOf("liebling", "likes_info")

  val followsError = withContentsOf("follows", "follow_failed_normal")
  val followsAgeRestrictedError = withContentsOf("follows", "follow_failed_age_restricted")
  val followsAgeUnknownError = withContentsOf("follows", "follow_failed_age_unknown")

  val okidokiFetch = withContentsOf("okidoki", "fetch")
  val okidokiUsers = withContentsOf("okidoki", "users")

  val trackCoordinatorUpdate = withContentsOf("track-coordinator", "track-update")
  val trackCoordinatorTrack = withContentsOf("track-coordinator", "track")
}
