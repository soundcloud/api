package com.soundcloud.publicApiStrangler.service.CreatedTrack

import com.soundcloud.publicApiStrangler.client.trackcoordinator.{PublisherMetadata, TrackCoordinatorTrack}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json.obj
import play.api.libs.json.{JsNull, JsValue, Json}

class CreatedTrackSpec extends Specification {
  "#writes" >> {
    trait WritesContext extends Scope {

      def public: Boolean = false

      def result: JsValue = {
        val trackCoordinatorTrack = TrackCoordinatorTrack(
          urn = "soundcloud:sounds:174088262",
          public = public,
          title = "Awesome Track",
          api_streamable = Some(true),
          commentable = true,
          description = Some("This track is awesome"),
          downloadable = Some(false),
          embeddable = Some(true),
          genre = Some("Free jazz"),
          geo_blockings = Some(List("US")),
          publisher_metadata = Some(PublisherMetadata(Some("US-S1Z-99-00001"))),
          label_name = Some("Foobar records"),
          license = "all-rights-reserved",
          permalink = "awesome-track-2014-10-27-17-25-29-66",
          purchase_title = Some("buy123"),
          purchase_url = Some("http://buy.that.com"),
          release_day = Some(1),
          release_month = Some(2),
          reveal_comments = true,
          reveal_stats = true,
          tag_list = Some("tag onw two \"hello tag\" tōkyō"),
          secret_token = Some("s-8USae"),
          uri = "https://api.soundcloud.com/tracks/174088262"
        )
        Json.toJson(CreatedTrack(trackCoordinatorTrack))
      }
    }

    "when the track is private" in new WritesContext {
      result ==== obj(
        "id" -> 174088262,
        "kind" -> "track",
        "permalink" -> "awesome-track-2014-10-27-17-25-29-66",
        "secret_token" -> "s-8USae",
        "urn" -> "soundcloud:sounds:174088262"
      )
    }

    "when the track is public" in new WritesContext {
      override def public: Boolean = true

      (result \ "secret_token").get ==== JsNull
    }
  }
}
