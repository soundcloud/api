package com.soudcloud.authorization

import com.soudcloud.data.ParsedValue
import com.soundcloud.jvmkit.policies.ContentPolicies
import com.soundcloud.scalakit.UserSession

class TrackPolicies(policy: ContentPolicies) {

  def apply(session: UserSession, track: ParsedValue): Option[ParsedValue] =
    if (policy != ContentPolicies.BLOCK)
      Some(track + policy)
    else
      None
}
