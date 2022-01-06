package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.util.Urn

object UserUrnUtil {
  def getUserUrn(userId: String): Urn = {
    val IdParamPattern = "^(\\d+)$".r
    userId match {
      case IdParamPattern(id) => Urn("soundcloud", "users", id)
      case other => throw new IllegalArgumentException(s"Invalid user id: '$other'")
    }
  }
}
