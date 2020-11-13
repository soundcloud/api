package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.client.mothership.response.mapper.UserMapper
import com.soundcloud.publicApiStrangler.client.trackcoordinator.TrackCoordinatorTrackFixtures
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json.obj
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue, Json}

class CreatedTrackSpec extends Specification {
  "#writes" >> {
    trait WritesContext extends Scope {

      def public: Boolean = false

      def agentUrn: Option[Urn] = None

      val users = Fixtures.okidokiUsers.as[List[JsObject]].map(UserMapper(_))
      val user = users.head
      val trackCoordinatorTrack = new TrackCoordinatorTrackFixtures().build(public)

      def result: JsValue = {
        Json.toJson(CreatedTrack(trackCoordinatorTrack, user, agentUrn))
      }
    }

    "when the track is private" in new WritesContext {
      val secretTokenParam = s"secret_token=${trackCoordinatorTrack.secret_token.get}"
      val secretUri = Some(s"${trackCoordinatorTrack.uri}?$secretTokenParam")
      val downloadUrl = s"${trackCoordinatorTrack.download_url}?$secretTokenParam"
      val streamUrl = s"${trackCoordinatorTrack.stream_url}?$secretTokenParam"
      val permalinkUrl = s"${trackCoordinatorTrack.permalink_url}/${trackCoordinatorTrack.secret_token.get}"

      result ==== obj(
        "artwork_url" -> trackCoordinatorTrack.artwork_url,
        "bpm" -> trackCoordinatorTrack.bpm,
        "comment_count" -> trackCoordinatorTrack.comment_count,
        "commentable" -> trackCoordinatorTrack.commentable,
        "created_at" -> trackCoordinatorTrack.created_at,
        "description" -> trackCoordinatorTrack.description,
        "download_count" -> trackCoordinatorTrack.downloads_count,
        "download_url" -> downloadUrl,
        "downloadable" -> trackCoordinatorTrack.downloadable,
        "duration" -> trackCoordinatorTrack.duration,
        "embeddable_by" -> trackCoordinatorTrack.embeddable_by,
        "favoritings_count" -> trackCoordinatorTrack.favoritings_count,
        "genre" -> trackCoordinatorTrack.genre,
        "id" -> Urn.parse(trackCoordinatorTrack.urn).get.identifier.toLong,
        "isrc" -> trackCoordinatorTrack.isrc,
        "key_signature" -> trackCoordinatorTrack.key_signature,
        "kind" -> "track",
        "label_id" -> trackCoordinatorTrack.label_id,
        "label_name" -> trackCoordinatorTrack.label_name,
        "last_modified" -> trackCoordinatorTrack.last_modified,
        "license" -> trackCoordinatorTrack.license,
        "original_content_size" -> trackCoordinatorTrack.original_content_size,
        "original_format" -> trackCoordinatorTrack.original_format,
        "permalink_url" -> permalinkUrl,
        "permalink" -> trackCoordinatorTrack.permalink,
        "playback_count" -> trackCoordinatorTrack.playback_count,
        "purchase_title" -> trackCoordinatorTrack.purchase_title,
        "purchase_url" -> trackCoordinatorTrack.purchase_url,
        "release" -> trackCoordinatorTrack.release,
        "release_day" -> trackCoordinatorTrack.release_day,
        "release_month" -> trackCoordinatorTrack.release_month,
        "release_year" -> trackCoordinatorTrack.release_year,
        "secret_token" -> trackCoordinatorTrack.secret_token,
        "secret_uri" -> secretUri,
        "sharing" -> trackCoordinatorTrack.sharing,
        "state" -> trackCoordinatorTrack.state,
        "stream_url" -> streamUrl,
        "streamable" -> trackCoordinatorTrack.streamable,
        "tag_list" -> trackCoordinatorTrack.tag_list,
        "title" -> trackCoordinatorTrack.title,
        "track_type" -> trackCoordinatorTrack.track_type,
        "uri" -> secretUri,
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
        "user_favorite" -> false,
        "user_id" -> Urn.parse(trackCoordinatorTrack.user_urn).get.identifier.toLong,
        "user_playback_count" -> trackCoordinatorTrack.user_playback_count,
        "video_url" -> trackCoordinatorTrack.video_url,
        "waveform_url" -> trackCoordinatorTrack.waveform_url
      )
    }

    "when the track is public" in new WritesContext {
      override def public: Boolean = true

      (result \ "secret_token").get ==== JsNull
      (result \ "secret_uri").get ==== JsNull
      (result \ "uri").get ==== JsString(trackCoordinatorTrack.uri)
      (result \ "download_url").get ==== JsString(trackCoordinatorTrack.download_url)
      (result \ "stream_url").get ==== JsString(trackCoordinatorTrack.stream_url)
      (result \ "permalink_url").get ==== JsString(trackCoordinatorTrack.permalink_url)
    }

    "when track is private and the client is Ableton application" >> {
      "should return permalink_url without secret token" in new WritesContext {
        override def agentUrn: Option[Urn] = Some(Urn("soundcloud", "applications", "45176"))

        (result \ "permalink_url").get ==== JsString(trackCoordinatorTrack.permalink_url)
      }
    }
  }
}
