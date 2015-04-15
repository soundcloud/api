package com.soundcloud.publicApiStrangler.rateLimitingOld

trait Consumer {
  def identifier: String

  override def toString: String = s"Consumer {identifier:$identifier}"
}
