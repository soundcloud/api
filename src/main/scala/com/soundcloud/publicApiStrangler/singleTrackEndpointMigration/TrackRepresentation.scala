package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.{Country, UserSession}
import com.soundcloud.publicApiStrangler.client.{DomainLocking, TrackAudioMetadata}
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
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
    case t: TrackRepresentation => TrackRepresentation.writes.writes(t)
  }

  implicit val userWrites = Writes[User] { user =>
    Json.obj(
      "id" -> user.urn.getIdentifier.toLong,
      "kind" -> "user",
      "permalink" -> user.permalink,
      "username" -> user.username,
      // last_modified
      "uri" -> s"https://api.soundcloud.com/users/${user.urn.getIdentifier}",
      "permalink_url" -> user.permalink_url,
      "avatar_url" -> user.avatar_url
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
      "user_favorite" -> dec.isLiked.toString
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
      "secret_url" -> s"https://api.soundcloud.com/tracks/${dec.track.urn.getIdentifier}?secret_token=${dec.track.secret_token}"
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
        "tag_list" -> (rep.track.user_tags ++ rep.track.machine_tags).mkString(", "),
        "permalink" -> rep.track.permalink,
        "streamable" -> rep.track.streamable,
        "embeddable_by" -> rep.track.embeddableBy,
        "downloadable" -> rep.track.downloadable,
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
        "bpm" -> rep.track.bpm,
        "release_year" -> rep.track.release_year,
        "release_month" -> rep.track.release_month,
        "release_day" -> rep.track.release_day,
        "original_format" -> rep.audioMetadata.original_format,
        "license" -> rep.track.license,
        "uri" -> s"https://api.soundcloud.com/tracks/${rep.id}",
        "user" -> rep.user,
        // user_favorite --> liebling
        "permalink_url" -> rep.track.permalink_url,
        "artwork_url" -> rep.track.artwork.filename.map(imageUrl(_)),
        // waveform_url --> media service
        "playback_count" -> rep.counts.playback_count,
        "download_count" -> rep.counts.download_count,
        "favoritings_count" -> rep.counts.favoritings_count,
        "comment_count" -> rep.counts.comment_count,

        // Conditional attributes (already exposed)
        // TODO: expose conditionally
        "stream_url" -> s"https://api.soundcloud.com/tracks/${rep.id}/stream",
        "download_url" -> s"https://api.soundcloud.com/tracks/${rep.id}/download"

        // Conditional attributes (not yet exposed)
        // created_with
        // attachments
        // attachments_uri
        // likes_count
        // reposts_count
        // downloads_remaining
        // artwork_url
        // user_favorite
        // user_playback_count
      )
    }

    private def imageUrl(imageFile: String): String = {
      val s3FilenamePattern = """(.*)-original\.(\w*)""".r

      imageFile match {
        case s3FilenamePattern(s3filename, extension) => cdnRoot + s"/$s3filename-large.$extension"
        case _ => cdnRoot + "/" + imageFile
      }
    }
  }
}
