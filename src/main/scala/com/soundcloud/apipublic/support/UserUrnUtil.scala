package com.soundcloud.apipublic.support

import com.soundcloud.jvmkit.module.util.Urn

object UserUrnUtil {
  def getUserUrn(userId: String): Urn = {
    Urn.parse(userId).getOrElse(Urn("soundcloud", "users", userId))
  }
}
