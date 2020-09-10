package com.soundcloud.publicApiStrangler.service.playlists

import com.soundcloud.jvmkit.module.util.Urn

case class PlaylistRequest(urn: Urn, secretToken: Option[String])
