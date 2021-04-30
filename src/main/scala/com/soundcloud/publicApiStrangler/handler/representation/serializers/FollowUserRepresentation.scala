package com.soundcloud.publicApiStrangler.handler.representation.serializers

import com.soundcloud.publicApiStrangler.client.mothership.response.representation.UserRepresentation
import play.api.libs.json.{Json, Writes}

object UserFollowRepresentation {
  implicit val userFollowWrites = Writes[UserRepresentation] { user =>
    Json.obj(
      "avatar_url" -> user.avatar_url.replaceAll("\\?[0-9]+$", "").replaceAll("^http:", "https:"),
      "id" -> user.urn.identifier.toLong,
      "kind" -> "user",
      "permalink_url" -> user.permalink_url,
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.identifier}",
      "username" -> user.username,
      "permalink" -> user.permalink,
      "last_modified" -> user.updated_at,
      "first_name" -> user.first_name,
      "last_name" -> user.last_name,
      "full_name" -> user.full_name,
      "city" -> user.city,
      "description" -> user.description,
      "country" -> user.country,
      "track_count" -> user.tracks_count,
      "public_favorites_count" -> user.public_favorites_count,
      "reposts_count" -> user.reposts_count,
      "followers_count" -> user.followers_count,
      "followings_count" -> user.followings_count,
      "plan" -> user.plan,
      "myspace_name" -> user.myspace_name,
      "discogs_name" -> user.discogs_name,
      "website_title" -> user.website_title,
      "website" -> user.website,
      "comments_count" -> user.comments_count,
      "online" -> user.online,
      "likes_count" -> user.likes_count,
      "playlist_count" -> user.public_playlists_count
    )
  }
}
