package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient

import com.soundcloud.scalakit.Urn
import com.soundcloud.scalakit.Urn.format
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
                 published_at: Option[LocalDateTime])

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
          (json \ "urn").as[Urn],
          (json \ "user_urn").as[Urn],
          (json \ "commentable").as[Boolean],
          (json \ "description").asOpt[String],
          (json \ "created_at").as[LocalDateTime],
          (json \ "disabled_at").asOpt[LocalDateTime],
          (json \ "downloadable").as[Boolean],
          (json \ "duration").as[Int],
          (json \ "genre").asOpt[String],
          (json \ "last_modified").as[LocalDateTime],
          (json \ "permalink").as[String],
          (json \ "permalink_url").asOpt[String],
          (json \ "public").as[Boolean],
          (json \ "secret_token").as[String],
          (json \ "user_tags").as[List[String]],
          (json \ "machine_tags").as[List[String]],
          (json \ "title").as[String],
          (json \ "uid").asOpt[String],
          (json \ "api_streamable").asOpt[Boolean],
          (json \ "streamable").as[Boolean],
          (json \ "reveal_comments").as[Boolean],
          (json \ "reveal_stats").as[Boolean],
          (json \ "label_name").asOpt[String],
          (json \ "license").as[String],
          (json \ "embeddable").asOpt[Boolean],
          (json \ "release_year").asOpt[Int],
          (json \ "release_month").asOpt[Int],
          (json \ "release_day").asOpt[Int],
          (json \ "embeddable_by").as[EmbeddingPermission],
          (json \ "release_date").asOpt[LocalDateTime],
          (json \ "artwork").as[Artwork],
          (json \ "published_at").asOpt[LocalDateTime]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}
