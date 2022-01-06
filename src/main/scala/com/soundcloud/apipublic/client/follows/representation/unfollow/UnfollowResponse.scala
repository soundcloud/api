package com.soundcloud.apipublic.client.follows.representation.unfollow

sealed trait UnfollowResponse

case object UnfollowSuccessful extends UnfollowResponse

sealed trait UnfollowingNotPossible extends UnfollowResponse
case object UserNotFound extends UnfollowingNotPossible
case object UserAsTarget extends UnfollowingNotPossible
case object NotFollowing extends UnfollowingNotPossible

final case class UnknownError(message: String, status: Int) extends UnfollowResponse
