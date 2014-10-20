package com.soundcloud.publicApiStrangler.rateLimiting

trait Consumer {
  def identifier: String

  override def toString: String = s"Consumer {identifier:$identifier}"
}
