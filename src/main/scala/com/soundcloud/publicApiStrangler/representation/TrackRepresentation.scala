package com.soundcloud.publicApiStrangler.representation

import java.net.URLEncoder

import com.soundcloud.jvmkit.{Country, Urn}
import com.soundcloud.publicApiStrangler.client.mediaservice.WaveformUrl
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.client.trackmetadata.Track
import com.soundcloud.publicApiStrangler.client.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.service.response.representation.{Geoblockings, User}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

sealed trait TrackRepresentationLike

object TrackRepresentationLike {
  implicit val writes: Writes[TrackRepresentationLike] = Writes[TrackRepresentationLike] {
    case t: TrackRepresentationSecretTokenDecorator => TrackRepresentationSecretTokenDecorator.writes.writes(t)
    case t: TrackRepresentationLabelDecorator => TrackRepresentationLabelDecorator.writes.writes(t)
    case t: TrackRepresentationGeoblockingsDecorator => TrackRepresentationGeoblockingsDecorator.writes.writes(t)
    case t: TrackRepresentationDomainLockingsDecorator => TrackRepresentationDomainLockingsDecorator.writes.writes(t)
    case t: TrackRepresentationUserFavoriteDecorator => TrackRepresentationUserFavoriteDecorator.writes.writes(t)
    case t: TrackRepresentationUserPlaybackCountDecorator => TrackRepresentationUserPlaybackCountDecorator.writes.writes(t)
    case t: TrackRepresentationWaveformUrlDecorator => TrackRepresentationWaveformUrlDecorator.writes.writes(t)
    case t: TrackRepresentationAttachmentsUriDecorator => TrackRepresentationAttachmentsUriDecorator.writes.writes(t)
    case t: TrackRepresentationSecretTokenUriParamDecorator => TrackRepresentationSecretTokenUriParamDecorator.writes.writes(t)
    case t: TrackRepresentationQuotaDecorator => TrackRepresentationQuotaDecorator.writes.writes(t)
    case t: TrackRepresentationCountsDecorator => TrackRepresentationCountsDecorator.writes.writes(t)
    case t: TrackRepresentationCommentCountDecorator => TrackRepresentationCommentCountDecorator.writes.writes(t)
    case t: TrackRepresentation => TrackRepresentation.writes.writes(t)
  }

  implicit val userWrites = Writes[User] { user =>
    Json.obj(
      "id" -> user.urn.getIdentifier.toLong,
      "kind" -> "user",
      "permalink" -> user.permalink,
      "username" -> user.username,
      "last_modified" -> user.updated_at,
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.getIdentifier}",
      "permalink_url" -> user.permalink_url,
      "avatar_url" -> user.avatar_url.replaceAll("\\?[0-9]+$", "").replaceAll("^http:", "https:")
    )
  }
}

case class TrackRepresentationCountsDecorator(
  counts: StitchCounts,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationCountsDecorator {
  implicit val writes = Writes[TrackRepresentationCountsDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "playback_count" -> dec.counts.playback_count,
      "download_count" -> dec.counts.download_count,
      "favoritings_count" -> dec.counts.favoritings_count
    )
  }
}

case class TrackRepresentationCommentCountDecorator(
  counts: StitchCounts,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationCommentCountDecorator {
  implicit val writes = Writes[TrackRepresentationCommentCountDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "comment_count" -> dec.counts.comment_count
    )
  }
}

case class TrackRepresentationUserPlaybackCountDecorator(
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationUserPlaybackCountDecorator {

  // This is also hard-coded to be 1 inside Mothership
  implicit val writes = Writes[TrackRepresentationUserPlaybackCountDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "user_playback_count" -> 1
    )
  }
}

case class TrackRepresentationUserFavoriteDecorator(
  isLiked: Boolean,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationUserFavoriteDecorator {
  implicit val writes = Writes[TrackRepresentationUserFavoriteDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "user_favorite" -> dec.isLiked
    )
  }
}

case class TrackRepresentationSecretTokenDecorator(
  track: Track,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationSecretTokenDecorator {
  implicit val writes = Writes[TrackRepresentationSecretTokenDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "secret_token" -> dec.track.secret_token,
      "secret_uri" -> s"https://api.soundcloud.com/tracks/${dec.track.urn.getIdentifier}?secret_token=${dec.track.secret_token}"
    )
  }
}

case class TrackRepresentationLabelDecorator(
  label: User,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationLabelDecorator {
  // FIXME: ugly to have to import it here
  import TrackRepresentationLike.userWrites

  implicit val writes = Writes[TrackRepresentationLabelDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "label" -> dec.label
    )
  }
}

case class TrackRepresentationGeoblockingsDecorator(
  geoblockings: Geoblockings,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationGeoblockingsDecorator {
  implicit val writes = Writes[TrackRepresentationGeoblockingsDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "available_country_codes" -> Country.officiallyAssignedAlpha2Codes.--(dec.geoblockings)
    )
  }
}

case class TrackRepresentationDomainLockingsDecorator(
  domainLockings: Seq[DomainLocking],
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationDomainLockingsDecorator {
  implicit val writes = Writes[TrackRepresentationDomainLockingsDecorator] { dec =>
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "domain_lockings" -> dec.domainLockings.map(dl => Json.obj("domain" -> dl.domain))
    )
  }
}

case class TrackRepresentationWaveformUrlDecorator(
  waveformUrls: Seq[WaveformUrl],
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationWaveformUrlDecorator {
  implicit val writes = Writes[TrackRepresentationWaveformUrlDecorator] { dec =>
    val attribute = dec.waveformUrls.find(_.label == "stream") match {
      case Some(url) => Json.obj("waveform_url" -> url.png)
      case _ => Json.obj()
    }
    Json.toJson(dec.wrapped).as[JsObject] ++ attribute
  }
}

case class TrackRepresentationAttachmentsUriDecorator(
  trackUrn: Urn,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationAttachmentsUriDecorator {
  implicit val writes = Writes[TrackRepresentationAttachmentsUriDecorator] { dec =>
    val id = dec.trackUrn.getIdentifier
    Json.toJson(dec.wrapped).as[JsObject] ++ Json.obj(
      "attachments_uri" -> s"https://api.soundcloud.com/tracks/${id}/attachments"
    )
  }
}

case class TrackRepresentationSecretTokenUriParamDecorator(
  wrapped: TrackRepresentationLike,
  secretParam: String
) extends TrackRepresentationLike

object TrackRepresentationSecretTokenUriParamDecorator {
  implicit val writes = Writes[TrackRepresentationSecretTokenUriParamDecorator] { dec =>
    val json = Json.toJson(dec.wrapped).as[JsObject]

    json ++ addSecretToFieldAsParam("uri", json, dec) ++
      addSecretToFieldAsParam("stream_url", json, dec) ++
      addSecretToFieldAsParam("download_url", json, dec) ++
      addSecretToFieldAsPath("permalink_url", json, dec)
  }

  private def addSecretToFieldAsParam(fieldName: String, json: JsObject, dec: TrackRepresentationSecretTokenUriParamDecorator): JsObject = {
    val secret = URLEncoder.encode(dec.secretParam, "UTF-8")
    (json \ fieldName).asOpt[String] match {
      case Some(value) => Json.obj(fieldName -> s"$value?secret_token=$secret")
      case None => Json.obj()
    }
  }

  private def addSecretToFieldAsPath(fieldName: String, json: JsObject, dec: TrackRepresentationSecretTokenUriParamDecorator): JsObject = {
    val secret = URLEncoder.encode(dec.secretParam, "UTF-8")
    (json \ fieldName).asOpt[String] match {
      case Some(value) => Json.obj(fieldName -> s"$value/$secret")
      case None => Json.obj()
    }
  }
}

case class TrackRepresentationQuotaDecorator(
  downloadable: Option[Boolean],
  downloadsPerTrack: Option[Int],
  downloadCount: Int,
  userIsOwner: Boolean,
  wrapped: TrackRepresentationLike
) extends TrackRepresentationLike

object TrackRepresentationQuotaDecorator {
  implicit val writes = Writes[TrackRepresentationQuotaDecorator] { dec =>

    val trackDownloadable = dec.downloadable.getOrElse(false)
    val downloadable: Boolean = (trackDownloadable, dec.downloadsPerTrack) match {
      case (false, _) => false
      case (true, None) => trackDownloadable // User has no quota, default to track's 'downloadable' setting
      case (true, Some(quota)) => dec.downloadCount < quota
    }
    val downloadableJson = Json.obj("downloadable" -> downloadable)

    val downloadsRemainingJson = dec.downloadsPerTrack.map(_ - dec.downloadCount) match {
      case Some(remaining) if dec.userIsOwner => Json.obj("downloads_remaining" -> remaining)
      case _ => Json.obj()
    }

    Json.toJson(dec.wrapped).as[JsObject] ++ downloadableJson ++ downloadsRemainingJson
  }
}

case class TrackRepresentation(
  track: Track,
  user: User,
  isrc: Option[Isrc],
  counts: StitchCounts,
  label: Option[User],
  geoblockings: Option[Geoblockings],
  domainlockings: Seq[DomainLocking],
  audioMetadata: TrackAudioMetadata) extends TrackRepresentationLike {
  def id = track.urn.getIdentifier.toLong
}

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")
  private val cdnRoot = "https://i1.sndcdn.com"

  implicit val writes = new Writes[TrackRepresentation] {
    // FIXME: ugly to have to import it here
    import TrackRepresentationLike.userWrites

    override def writes(rep: TrackRepresentation): JsValue = {
      Json.obj(
        "kind" -> "track",
        "id" -> rep.id,
        "created_at" -> rep.track.created_at.toString(dateTimeFormat),
        "user_id" -> rep.user.urn.getIdentifier.toLong,
        "duration" -> rep.track.duration,
        "commentable" -> rep.track.commentable,
        "state" -> rep.audioMetadata.state,
        "original_content_size" -> rep.audioMetadata.original_content_size,
        "last_modified" -> rep.track.last_modified.toString(dateTimeFormat),
        "sharing" -> (if (rep.track.public) "public" else "private"),
        "tag_list" -> mkTagList(rep),
        "permalink" -> rep.track.permalink,
        "streamable" -> rep.track.api_streamable,
        "embeddable_by" -> rep.track.embeddableBy,
        "purchase_url" -> rep.track.purchase_url,
        "purchase_title" -> rep.track.purchase_title,
        "label_id" -> rep.track.label_id,
        "genre" -> rep.track.genre,
        "title" -> rep.track.title,
        "description" -> rep.track.description,
        "label_name" -> rep.track.label_name,
        "release" -> rep.track.release,
        "track_type" -> rep.track.track_type,
        "key_signature" -> rep.track.key_signature,
        "isrc" -> rep.isrc.map(_.toString),
        "video_url" -> rep.track.video_url,
        "bpm" -> rep.track.bpm.map(roundBpm(_)),
        "release_year" -> rep.track.release_year,
        "release_month" -> releaseMonthFor(rep),
        "release_day" -> releaseDayFor(rep),
        "original_format" -> rep.audioMetadata.original_format,
        "license" -> rep.track.license,
        "uri" -> urlFor(rep),
        "user" -> rep.user,
        "permalink_url" -> rep.track.permalink_url,
        "artwork_url" -> rep.track.artwork.filename.map(imageUrl(_)),
        "stream_url" -> urlFor(rep, "stream"),
        "download_url" -> urlFor(rep, "download")
      )
    }

    private val baseUrl = "https://api.soundcloud.com/tracks"

    private def urlFor(rep: TrackRepresentation) = s"${baseUrl}/${rep.id}"

    private def urlFor(rep: TrackRepresentation, subresource: String) = s"${baseUrl}/${rep.id}/$subresource"

    private def roundBpm(f: Double): Double =
      (f * 10000.0).round.toDouble / 10000.0

    private def mkTagList(rep: TrackRepresentation): String =
      (rep.track.machine_tags ++ rep.track.user_tags).map(quoteTagIfNecessary _).mkString(" ")

    private def quoteTagIfNecessary(tag: String): String =
      if (tag.exists(_.isSpaceChar))
        "\"" + tag + "\""
      else
        tag

    private def releaseDayFor(rep: TrackRepresentation): Option[Int] =
      rep.track.release_year.map(_ => rep.track.release_day.getOrElse(1))

    private def releaseMonthFor(rep: TrackRepresentation): Option[Int] =
      rep.track.release_year.map(_ => rep.track.release_month.getOrElse(1))

    private def imageUrl(imageFile: String): String = {
      val s3FilenamePattern = """(.*)-original\.\w*""".r

      imageFile match {
        case s3FilenamePattern(s3filename) => cdnRoot + s"/$s3filename-large.jpg"
        case _ => cdnRoot + "/" + imageFile
      }
    }
  }
}
