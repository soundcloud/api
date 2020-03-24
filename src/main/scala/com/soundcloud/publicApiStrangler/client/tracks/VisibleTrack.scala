package com.soundcloud.publicApiStrangler.client.tracks

import com.soundcloud.api.partners.clients.tracks.Transcoding
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.authorization.policies._
import com.soundcloud.publicApiStrangler.client.trackmetadata.{Artwork, EmbeddingPermission}
import org.joda.time.LocalDateTime
import play.api.libs.json._

case class VisibleTrack(
    urn: Urn,
    userUrn: Urn,
    uid: Option[String],
    title: String,
    createdAt: LocalDateTime,
    disabledAt: Option[LocalDateTime],
    lastModified: LocalDateTime,
    downloadable: Boolean,
    duration: Int,
    commentable: Boolean,
    genre: Option[String],
    public: Boolean,
    permalink: String,
    permalinkUrl: Option[String],
    userTags: List[String],
    description: Option[String],
    secretToken: Option[String],
    revealStats: Boolean,
    artwork: Artwork,
    publishedAt: Option[LocalDateTime],
    machineTags: List[String],
    streamable: Boolean,
    apiStreamable: Option[Boolean],
    revealComments: Boolean,
    labelName: Option[String],
    license: String,
    embeddable: Option[Boolean],
    releaseYear: Option[Int],
    releaseMonth: Option[Int],
    releaseDay: Option[Int],
    embeddableBy: EmbeddingPermission,
    releaseDate: Option[LocalDateTime],
    purchaseUrl: Option[String],
    purchaseTitle: Option[String],
    authorization: ContentAuthorization,
    transcodings: List[Transcoding],
    waveformUrls: List[WaveformUrl]
)

object VisibleTrack {
  implicit val jodaISODateReads: Reads[org.joda.time.LocalDateTime] = new Reads[org.joda.time.LocalDateTime] {
    def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(s) => JsSuccess(LocalDateTime.parse(s))
      case _ => JsError(Seq(JsPath() -> Seq(JsonValidationError("Could not parse datetime value"))))
    }
  }

  implicit val reads: Reads[VisibleTrack] = Reads { json =>
    try {
      JsSuccess(
        VisibleTrack(
          (json \ "urn").as[Urn],
          (json \ "userUrn").as[Urn],
          (json \ "uid").asOpt[String],
          (json \ "title").as[String],
          (json \ "createdAt").as[LocalDateTime],
          (json \ "disabledAt").asOpt[LocalDateTime],
          (json \ "lastModified").as[LocalDateTime],
          (json \ "downloadable").as[Boolean],
          (json \ "duration").as[Int],
          (json \ "commentable").as[Boolean],
          (json \ "genre").asOpt[String],
          (json \ "public").as[Boolean],
          (json \ "permalink").as[String],
          (json \ "permalinkUrl").asOpt[String],
          (json \ "userTags").as[List[String]],
          (json \ "description").asOpt[String],
          (json \ "secretToken").asOpt[String],
          (json \ "revealStats").as[Boolean],
          (json \ "artwork").as[Artwork],
          (json \ "publishedAt").asOpt[LocalDateTime],
          (json \ "machineTags").as[List[String]],
          (json \ "streamable").as[Boolean],
          (json \ "apiStreamable").asOpt[Boolean],
          (json \ "revealComments").as[Boolean],
          (json \ "labelName").asOpt[String],
          (json \ "license").as[String],
          (json \ "embeddable").asOpt[Boolean],
          (json \ "releaseYear").asOpt[Int],
          (json \ "releaseMonth").asOpt[Int],
          (json \ "releaseDay").asOpt[Int],
          (json \ "embeddableBy").as[EmbeddingPermission],
          (json \ "releaseDate").asOpt[LocalDateTime],
          (json \ "purchaseUrl").asOpt[String],
          (json \ "purchaseTitle").asOpt[String],
          new ContentAuthorization(
            (json \ "urn").as[Urn],
            ContentPolicy.from((json \ "authorization" \ "policy").as[String]),
            Reason.from((json \ "authorization" \ "reason").as[String]),
            (json \ "authorization" \ "restrictions").as[Set[String]].map(ContentRestriction.from(_)),
            MonetizationModel.from((json \ "authorization" \ "monetizationModel").as[String])
          ),
          (json \ "transcodings").as[List[Transcoding]],
          (json \ "waveformUrls").as[List[WaveformUrl]]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}
