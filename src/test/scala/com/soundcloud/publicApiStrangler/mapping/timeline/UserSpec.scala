package com.soundcloud.publicApiStrangler.mapping.timeline

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.bff.test.UnitSpecification

class UserSpec extends UnitSpecification {
  """exposes "deprecated" count fields from Okidoki""" in new Scope {
    implicit val context = mock[MappingContext]

    val json = withContentsOf("okidoki", "users_with_deprecated_counts")(0)
    val baseUrl = ""
    val maybeFollowCounts = None

    val user = new User(json, baseUrl, maybeFollowCounts)

    user.playlist_count ==== Some(10001)
    user.likes_count ==== Some(10010)
    user.comments_count ==== Some(10100)
    user.reposts_count ==== Some(11000)
  }
}
