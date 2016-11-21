package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient

import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.Urn.format
import com.soundcloud.publicApiStrangler.support.HtmlSanitizer
import org.joda.time.LocalDateTime
import play.api.data.validation.ValidationError
import play.api.libs.json._

case class Track(urn: Urn,
                 user_urn: Urn,
                 commentable: Boolean,
                 description: Option[String],
                 created_at: LocalDateTime,
                 disabled_at: Option[LocalDateTime],
                 downloadable: Boolean,
                 duration: Int,
                 genre: Option[String],
                 last_modified: LocalDateTime,
                 permalink: String,
                 permalink_url: Option[String],
                 public: Boolean,
                 secret_token: String,
                 user_tags: List[String],
                 machine_tags: List[String],
                 title: String,
                 uid: Option[String],
                 api_streamable: Option[Boolean],
                 streamable: Boolean,
                 reveal_comments: Boolean,
                 reveal_stats: Boolean,
                 label_name: Option[String],
                 license: String,
                 embeddable: Option[Boolean],
                 release_year: Option[Int],
                 release_month: Option[Int],
                 release_day: Option[Int],
                 embeddableBy: EmbeddingPermission,
                 releaseDate: Option[LocalDateTime],
                 artwork: Artwork,
                 published_at: Option[LocalDateTime],
                 purchase_url: Option[String],
                 purchase_title: Option[String],
                 bpm: Option[Double],
                 track_type: Option[String],
                 release: Option[String],
                 key_signature: Option[String],
                 video_url: Option[String],
                 label_id: Option[Int])

case class Artwork(filename: Option[String])

object Artwork {
  implicit val format = Json.format[Artwork]
}

object Track {
  implicit val jodaISODateReads: Reads[org.joda.time.LocalDateTime] = new Reads[org.joda.time.LocalDateTime] {
    def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(s) => JsSuccess(LocalDateTime.parse(s))
      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Could not parse datetime value"))))
    }
  }

  implicit val trackReads: Reads[Track] = Reads { json =>
    try {
      JsSuccess(
        Track(
          urn = (json \ "urn").as[Urn],
          user_urn = (json \ "user_urn").as[Urn],
          commentable = (json \ "commentable").as[Boolean],
          description = (json \ "description").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          created_at = (json \ "created_at").as[LocalDateTime],
          disabled_at = (json \ "disabled_at").asOpt[LocalDateTime],
          downloadable = (json \ "downloadable").as[Boolean],
          duration = (json \ "duration").as[Int],
          genre = (json \ "genre").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          last_modified = (json \ "last_modified").as[LocalDateTime],
          permalink = (json \ "permalink").as[String],
          permalink_url = (json \ "permalink_url").asOpt[String],
          public = (json \ "public").as[Boolean],
          secret_token = (json \ "secret_token").as[String],
          user_tags = (json \ "user_tags").as[List[String]],
          machine_tags = (json \ "machine_tags").as[List[String]],
          title = HtmlSanitizer.sanitize((json \ "title").as[String]),
          uid = (json \ "uid").asOpt[String],
          api_streamable = (json \ "api_streamable").asOpt[Boolean],
          streamable = (json \ "api_streamable").as[Boolean],
          reveal_comments = (json \ "reveal_comments").as[Boolean],
          reveal_stats = (json \ "reveal_stats").as[Boolean],
          label_name = (json \ "label_name").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          license = (json \ "license").as[String],
          embeddable = (json \ "embeddable").asOpt[Boolean],
          release_year = (json \ "release_year").asOpt[Int],
          release_month = (json \ "release_month").asOpt[Int],
          release_day = (json \ "release_day").asOpt[Int],
          embeddableBy = (json \ "embeddable_by").as[EmbeddingPermission],
          releaseDate = (json \ "release_date").asOpt[LocalDateTime],
          artwork = (json \ "artwork").as[Artwork],
          published_at = (json \ "published_at").asOpt[LocalDateTime],
          purchase_url = (json \ "purchase_url").asOpt[String],
          purchase_title = (json \ "purchase_title").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          bpm = (json \ "bpm").asOpt[Double],
          track_type = (json \ "track_type").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          release = (json \ "release").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          key_signature = (json \ "key_signature").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          video_url = (json \ "video_url").asOpt[String],
          label_id = (json \ "label_id").asOpt[Int]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}
