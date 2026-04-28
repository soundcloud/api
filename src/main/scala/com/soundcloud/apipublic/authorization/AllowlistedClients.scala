package com.soundcloud.apipublic.authorization

import com.soundcloud.jvmkit.module.util.Urn

object AllowlistedClients {
  // The following client applications can check access for paywall'ed content
  val clients: Set[Urn] = Set(
    Urn("soundcloud", "applications", "167582"), // HEOS by Denon (Production)
    Urn("soundcloud", "applications", "59007"), // Soundiiz
    Urn("soundcloud", "applications", "62023"), // Soundiiz Local
    Urn("soundcloud", "applications", "329915") // SoundCloud Discord Bot (new)
  )
}
