package com.soundcloud.bff.test.fixtures

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

  val okidokiFetch = withContentsOf("okidoki", "fetch")
}
