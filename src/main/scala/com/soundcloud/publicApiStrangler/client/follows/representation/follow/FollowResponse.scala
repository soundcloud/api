package com.soundcloud.publicApiStrangler.client.follows.representation.follow

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.client.follows.representation.Following
import play.api.libs.json.JsObject

sealed trait FollowResponse

final case class FollowingCreated(following: Following) extends FollowResponse
case object AlreadyFollowing extends FollowResponse

sealed trait FollowingNotPossible extends FollowResponse

case object UserNotFound extends FollowingNotPossible
case class SpamBlocked(spamWarning: List[JsObject]) extends FollowingNotPossible
case object MaxFollowingsReached extends FollowingNotPossible
case object UserAsTarget extends FollowingNotPossible
case object BlockedByTarget extends FollowingNotPossible
case object AgeRestrictedUser extends FollowingNotPossible
case object AgeUnknownUser extends FollowingNotPossible
case class BulkFollowFailed(targets: List[Urn]) extends FollowingNotPossible

final case class UnknownError(message: String, status: Int) extends FollowResponse
