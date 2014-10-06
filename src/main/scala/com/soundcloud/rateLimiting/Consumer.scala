package com.soundcloud.rateLimiting

trait Consumer {
  def identifier: String

  override def toString: String = s"Consumer {identifier:$identifier}"
}
