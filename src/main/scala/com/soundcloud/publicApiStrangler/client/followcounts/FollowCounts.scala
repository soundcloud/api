package com.soundcloud.publicApiStrangler.client.followcounts

import com.soundcloud.jvmkit.module.util.Urn


/** Describes count information for a given user.
  *
  * @param userUrn
  * @param followers  The number of users following `userUrn`
  * @param followings The number of users followed by `userUrn`
  */
case class FollowCounts(userUrn: Urn, followers: Long, followings: Long)
