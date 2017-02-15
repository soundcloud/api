package com.soundcloud.publicApiStrangler.client.sketchy

sealed trait SketchyResponse
case object AckOk extends SketchyResponse
case object WarningNotFound extends SketchyResponse
case class UnknownError(status: Int, message: String) extends SketchyResponse
