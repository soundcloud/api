package com.soundcloud.publicApiStrangler.authorization

object TrackDurationActionStatus extends Enumeration {
  type TrackDurationActionStatus = Value
  val NeedsModification, DoesNotNeedModification = Value
}
