package com.soundcloud.publicApiStrangler.client.mothership.response.representation

sealed trait ResetUserPasswordResponse

case object OkResetUserPasswordResponse extends ResetUserPasswordResponse

// Don't expose this outside of SC! Should always return as success (e.g. 202 - Accepted) to user
case object UserDoesntExistButDontExposeThisResetUserPasswordResponse extends ResetUserPasswordResponse
