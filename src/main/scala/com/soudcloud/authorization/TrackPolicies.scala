package com.soudcloud.authorization

import com.soudcloud.data.{ParsedValue}
import com.soundcloud.bff.authorization.Policies
import com.soundcloud.bff.authorization.Policies.blocked
import com.soundcloud.scalakit.UserSession

class TrackPolicies(policies: Policies) {

  def apply(session: UserSession, track: ParsedValue): Option[ParsedValue] =
    if (policies.playback != blocked && policies.metadata != blocked)
      Some(track + policies)
    else
      None
}
