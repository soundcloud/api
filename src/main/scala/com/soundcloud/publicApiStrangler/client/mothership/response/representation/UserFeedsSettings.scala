package com.soundcloud.publicApiStrangler.client.mothership.response.representation

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.Json
import com.soundcloud.publicApiStrangler.client.support.CommonJsonFormats.urnFormat

case class FeedCategory(urn: Urn, name: Option[String])

object FeedCategory {
  implicit val format = Json.format[FeedCategory]
}

case class FeedEmail(urn: Urn, address: Option[String])

object FeedEmail {
  implicit val format = Json.format[FeedEmail]
}

case class UserFeedsSettings(
                              custom_author_name: Option[String],
                              custom_feed_title: Option[String],
                              custom_rss_feed_url: Option[String],
                              default_tracks_feedable: Boolean,
                              feeds_enabled: Option[Boolean],
                              is_explicit: Boolean,
                              language: Option[String],
                              redirect_url: Option[String],
                              feed_category: Option[FeedCategory],
                              email: Option[FeedEmail]
                            )

object UserFeedsSettings {
  implicit val format = Json.format[UserFeedsSettings]
}
