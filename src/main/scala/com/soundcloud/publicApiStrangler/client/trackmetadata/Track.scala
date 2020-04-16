package com.soundcloud.publicApiStrangler.client.trackmetadata

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.jvmkit.module.json.UrnFormat._
import com.soundcloud.publicApiStrangler.support.HtmlSanitizer
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JodaReads._
import com.soundcloud.publicApiStrangler.client.tracks.VisibleTrack

case class Track(
    urn: Urn,
    user_urn: Urn,
    commentable: Boolean,
    description: Option[String],
    created_at: DateTime,
    disabled_at: Option[DateTime],
    downloadable: Option[Boolean],
    duration: Int,
    genre: Option[String],
    last_modified: DateTime,
    permalink: String,
    permalink_url: Option[String],
    public: Boolean,
    secret_token: String,
    user_tags: List[String],
    machine_tags: List[String],
    title: String,
    uid: Option[String],
    api_streamable: Option[Boolean],
    streamable: Option[Boolean],
    reveal_comments: Boolean,
    reveal_stats: Boolean,
    label_name: Option[String],
    license: String,
    embeddable: Option[Boolean],
    release_year: Option[Int],
    release_month: Option[Int],
    release_day: Option[Int],
    embeddableBy: EmbeddingPermission,
    releaseDate: Option[DateTime],
    artwork: Artwork,
    published_at: Option[DateTime],
    purchase_url: Option[String],
    purchase_title: Option[String],
    bpm: Option[Double],
    track_type: Option[String],
    release: Option[String],
    key_signature: Option[String],
    video_url: Option[String],
    label_id: Option[Long],
    supply_chain_status: Option[String]
)

case class Artwork(filename: Option[String])

object Artwork {
  implicit val format = Json.format[Artwork]
}

object Track {
  def fromVisibleTrack(visibleTrack: VisibleTrack): Track = {
    Track(
      urn = visibleTrack.urn,
      user_urn = visibleTrack.userUrn,
      commentable = visibleTrack.commentable,
      description = visibleTrack.description,
      created_at = visibleTrack.createdAt.toDateTime,
      disabled_at = visibleTrack.disabledAt.map(_.toDateTime),
      downloadable = Some(visibleTrack.downloadable),
      duration = visibleTrack.duration,
      genre = visibleTrack.genre,
      last_modified = visibleTrack.lastModified.toDateTime,
      permalink = visibleTrack.permalink,
      permalink_url = visibleTrack.permalinkUrl,
      public = visibleTrack.public,
      secret_token = visibleTrack.secretToken.get,
      user_tags = visibleTrack.userTags,
      machine_tags = visibleTrack.machineTags,
      title = visibleTrack.title,
      uid = visibleTrack.uid,
      api_streamable = visibleTrack.apiStreamable,
      streamable = Some(visibleTrack.streamable),
      reveal_comments = visibleTrack.revealComments,
      reveal_stats = visibleTrack.revealStats,
      label_name = visibleTrack.labelName,
      license = visibleTrack.license,
      embeddable = visibleTrack.embeddable,
      release_year = visibleTrack.releaseYear,
      release_month = visibleTrack.releaseMonth,
      release_day = visibleTrack.releaseDay,
      embeddableBy = visibleTrack.embeddableBy,
      releaseDate = visibleTrack.releaseDate.map(_.toDateTime),
      artwork = visibleTrack.artwork,
      published_at = visibleTrack.publishedAt.map(_.toDateTime),
      purchase_url = visibleTrack.purchaseUrl,
      purchase_title = visibleTrack.purchaseTitle,
      bpm = visibleTrack.bpm,
      track_type = visibleTrack.trackType,
      release = visibleTrack.release,
      key_signature = visibleTrack.keySignature,
      video_url = visibleTrack.videoUrl,
      label_id = visibleTrack.labelId,
      supply_chain_status = visibleTrack.supplyChainStatus
    )
  }

  implicit val trackReads: Reads[Track] = Reads { json =>
    try {
      JsSuccess(
        Track(
          urn = (json \ "urn").as[Urn],
          user_urn = (json \ "user_urn").as[Urn],
          commentable = (json \ "commentable").as[Boolean],
          description = (json \ "description").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          created_at = (json \ "created_at").as[DateTime],
          disabled_at = (json \ "disabled_at").asOpt[DateTime],
          downloadable = (json \ "downloadable").asOpt[Boolean],
          duration = (json \ "duration").as[Int],
          genre = (json \ "genre").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          last_modified = (json \ "last_modified").as[DateTime],
          permalink = (json \ "permalink").as[String],
          permalink_url = (json \ "permalink_url").asOpt[String],
          public = (json \ "public").as[Boolean],
          secret_token = (json \ "secret_token").as[String],
          user_tags = (json \ "user_tags").as[List[String]],
          machine_tags = (json \ "machine_tags").as[List[String]],
          title = HtmlSanitizer.sanitize((json \ "title").as[String]),
          uid = (json \ "uid").asOpt[String],
          api_streamable = (json \ "api_streamable").asOpt[Boolean],
          streamable = (json \ "api_streamable").asOpt[Boolean],
          reveal_comments = (json \ "reveal_comments").as[Boolean],
          reveal_stats = (json \ "reveal_stats").as[Boolean],
          label_name = (json \ "label_name").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          license = (json \ "license").as[String],
          embeddable = (json \ "embeddable").asOpt[Boolean],
          release_year = (json \ "release_year").asOpt[Int],
          release_month = (json \ "release_month").asOpt[Int],
          release_day = (json \ "release_day").asOpt[Int],
          embeddableBy = (json \ "embeddable_by").as[EmbeddingPermission],
          releaseDate = (json \ "release_date").asOpt[DateTime],
          artwork = (json \ "artwork").as[Artwork],
          published_at = (json \ "published_at").asOpt[DateTime],
          purchase_url = (json \ "purchase_url").asOpt[String],
          purchase_title = (json \ "purchase_title").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          bpm = (json \ "bpm").asOpt[Double],
          track_type = (json \ "track_type").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          release = (json \ "release").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          key_signature = (json \ "key_signature").asOpt[String].map(HtmlSanitizer.sanitize(_)),
          video_url = (json \ "video_url").asOpt[String],
          label_id = (json \ "label_id").asOpt[Long],
          supply_chain_status = (json \ "supply_chain_status").asOpt[String]
        )
      )
    } catch {
      case ex: Exception => JsError(ex.getMessage)
    }
  }
}
