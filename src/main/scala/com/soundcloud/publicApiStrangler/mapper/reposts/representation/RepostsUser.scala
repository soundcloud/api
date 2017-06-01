package com.soundcloud.publicApiStrangler.mapper.reposts.representation

import com.soundcloud.bff.nextbff.mapping.MappingContext
import com.soundcloud.publicApiStrangler.client.followcounts.FollowCounts
import com.soundcloud.publicApiStrangler.client.liebling.UserTotalLikes
import com.soundcloud.publicApiStrangler.mapper.timeline.representation.{User => TimelineUser}
import play.api.libs.json.{JsValue, Json, Writes}

case class RepostsUser(jsonValue: JsValue,
                       baseUrl: String,
                       maybeFollowCounts: Option[FollowCounts],
                       maybeRepostsCount: Option[Long],
                       maybeLikesCount: Option[UserTotalLikes])(implicit context: MappingContext)
  extends TimelineUser(jsonValue, baseUrl, maybeFollowCounts, maybeRepostsCount) {

  override val public_favorites_count = maybeLikesCount.map(likesCountFor).orElse((json \ "public_favorites_count").asOpt[Long])
  override val likes_count = maybeLikesCount.map(likesCountFor).orElse((json \ "public_favorites_count").asOpt[Long])

  private def likesCountFor(likes: UserTotalLikes): Long =
    likes.track_likes_count + likes.playlist_likes_count
}

object RepostsUser {
  implicit val writes = new Writes[RepostsUser] {
    override def writes(u: RepostsUser): JsValue =
      Json.obj(
        "avatar_url" -> u.avatar_url,
        "id" -> u.id,
        "kind" -> u.kind,
        "permalink_url" -> u.permalink_url,
        "uri" -> u.uri,
        "username" -> u.username,
        "permalink" -> u.permalink,
        "last_modified" -> u.last_modified,
        "first_name" -> u.first_name,
        "last_name" -> u.last_name,
        "full_name" -> u.full_name,
        "city" -> u.city,
        "description" -> u.description,
        "country" -> u.country,
        "track_count" -> u.track_count,
        "public_favorites_count" -> u.public_favorites_count,
        "followers_count" -> u.followers_count,
        "followings_count" -> u.followings_count,
        "plan" -> u.plan,
        "myspace_name" -> u.myspace_name,
        "discogs_name" -> u.discogs_name,
        "website_title" -> u.website_title,
        "website" -> u.website,
        "reposts_count" -> u.reposts_count,
        "comments_count" -> u.comments_count,
        "online" -> u.online,
        "likes_count" -> u.likes_count,
        "playlist_count" -> u.playlist_count
      )
  }
}
