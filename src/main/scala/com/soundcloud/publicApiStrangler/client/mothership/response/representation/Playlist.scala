package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn

case class Playlist(urn: Urn,
                    user_urn: Urn,
                    title: String,
                    permalink: String,
                    description: Option[String],
                    created_at: String,
                    duration: Long,
                    genre: String,
                    permalink_url: String,
                    artwork_url: String,
                    track_count: Int,
                    likes_count: Int,
                    reposts_count: Int,
                    sharing: String,
                    tag_list: String)
