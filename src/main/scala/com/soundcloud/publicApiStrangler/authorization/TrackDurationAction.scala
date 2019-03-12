package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.TrackDurationActionStatus.TrackDurationActionStatus

/**
  * Indicates if for a given track urn we have to take action for replacing the duration.
  *
  * @param urn        Track urn.
  * @param status     Action.
  * @param durationMs Optional duration in milliseconds.
  */
case class TrackDurationAction(urn: Urn, status: TrackDurationActionStatus, durationMs: Option[Int])

object TrackDurationAction {
  val snipDuration = 30000
}