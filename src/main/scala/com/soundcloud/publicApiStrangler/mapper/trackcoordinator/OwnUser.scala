package com.soundcloud.publicApiStrangler.mapper.trackcoordinator

case class OwnUser(id: Long,
                   kind: String = "user",
                   permalink: String,
                   username: String,
                   last_modified: Option[String],
                   uri: String,
                   permalink_url: String,
                   avatar_url: Option[String])
