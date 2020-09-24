package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserMapper
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrackFixtures
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json.obj
import play.api.libs.json.{JsNull, JsObject, JsValue, Json}

class CreatedTrackSpec extends Specification {
  "#writes" >> {
    trait WritesContext extends Scope {

      def public: Boolean = false
      val users = Fixtures.okidokiUsers.as[List[JsObject]].map(UserMapper(_))
      val user = users.head
      val trackCoordinatorTrack = new TrackCoordinatorTrackFixtures().build(public)
      def result: JsValue = {
        Json.toJson(CreatedTrack(trackCoordinatorTrack, user))
      }
    }

    "when the track is private" in new WritesContext {
      result ==== obj(
        "artwork_url" -> trackCoordinatorTrack.artwork_url,
        "commentable" -> trackCoordinatorTrack.commentable,
        "created_at" -> trackCoordinatorTrack.created_at,
        "description" -> trackCoordinatorTrack.description,
        "download_count" -> trackCoordinatorTrack.downloads_count,
        "download_url" -> trackCoordinatorTrack.download_url,
        "downloadable" -> trackCoordinatorTrack.downloadable,
        "duration" -> trackCoordinatorTrack.duration,
        "embeddable_by" -> trackCoordinatorTrack.embeddable_by,
        "favoritings_count" -> trackCoordinatorTrack.favoritings_count,
        "genre" -> trackCoordinatorTrack.genre,
        "id" -> Urn.parse(trackCoordinatorTrack.urn).get.identifier.toLong,
        "isrc" -> trackCoordinatorTrack.isrc,
        "kind" -> "track",
        "label_id" -> trackCoordinatorTrack.label_id,
        "label_name" -> trackCoordinatorTrack.label_name,
        "last_modified" -> trackCoordinatorTrack.last_modified,
        "license" -> trackCoordinatorTrack.license,
        "original_content_size" -> trackCoordinatorTrack.original_content_size,
        "original_format" -> trackCoordinatorTrack.original_format,
        "permalink_url" -> trackCoordinatorTrack.permalink_url,
        "permalink" -> trackCoordinatorTrack.permalink,
        "playback_count" -> trackCoordinatorTrack.playback_count,
        "purchase_title" -> trackCoordinatorTrack.purchase_title,
        "purchase_url" -> trackCoordinatorTrack.purchase_url,
        "release_day" -> trackCoordinatorTrack.release_day,
        "release_month" -> trackCoordinatorTrack.release_month,
        "release_year" -> trackCoordinatorTrack.release_year,
        "secret_token" -> trackCoordinatorTrack.secret_token,
        "sharing" -> trackCoordinatorTrack.sharing,
        "state" -> trackCoordinatorTrack.state,
        "stream_url" -> trackCoordinatorTrack.stream_url,
        "streamable" -> trackCoordinatorTrack.streamable,
        "tag_list" -> trackCoordinatorTrack.tag_list,
        "title" -> trackCoordinatorTrack.title,
        "track_type" -> trackCoordinatorTrack.track_type,
        "uri" -> trackCoordinatorTrack.uri,
        "user" -> obj(
          "avatar_url" -> user.avatar_url,
          "id" -> user.urn.identifier.toLong,
          "kind" -> "user",
          "permalink_url" -> user.permalink_url,
          "uri" -> s"https://api.soundcloud.com/users/${user.urn.identifier}",
          "username" -> user.username,
          "permalink" -> user.permalink,
          "last_modified" -> user.updated_at
        ),
        "user_id" -> Urn.parse(trackCoordinatorTrack.user_urn).get.identifier.toLong,
        "waveform_url" -> trackCoordinatorTrack.waveform_url
      )
    }

    "when the track is public" in new WritesContext {
      override def public: Boolean = true

      (result \ "secret_token").get ==== JsNull
    }
  }
}
