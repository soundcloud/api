package com.soudcloud.authorization

import com.soundcloud.bff.test.UnitSpecification

trait Fixtures {
  this: UnitSpecification =>

  val singleTrack = withContentsOf("public_api", "single_track")
  val tracksArray = withContentsOf("public_api", "tracks_array")
  val playlist = withContentsOf("public_api", "playlist")
  val user = withContentsOf("public_api", "user")
}
