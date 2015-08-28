package com.soundcloud.publicApiStrangler.test.fixtures

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.Urn
import com.soundcloud.service.response.representation.liebling.LikesCount

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

  val lieblingLikesCounts = List(
    LikesCount(Urn("soundcloud:tracks:1"), 2L),
    LikesCount(Urn("soundcloud:playlists:123"), 666L),
    LikesCount(Urn("soundcloud:tracks:3"), 994L)
  )

  val followsError = withContentsOf("follows", "follow_failed_normal")
  val followsAgeRestrictedError = withContentsOf("follows", "follow_failed_age_restricted")
  val followsAgeUnknownError = withContentsOf("follows", "follow_failed_age_unknown")

  val okidokiFetch = withContentsOf("okidoki", "fetch")
  val okidokiUsers = withContentsOf("okidoki", "users")
}
