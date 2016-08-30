package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Country
import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.client.stitch.StitchCounts
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import com.soundcloud.service.response.representation.{Geoblockings, User}
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

case class TrackRepresentation(
  track: Track,
  user: User,
  isrc: Option[Isrc],
  counts: StitchCounts,
  label: Option[User],
  geoblockings: Option[Geoblockings]) {
  def id = track.urn.getIdentifier.toLong
}

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")

  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue = {
      val coreObject = Json.obj(
        "kind" -> "track",
        "id" -> rep.id,
        "created_at" -> rep.track.created_at.toString(dateTimeFormat),
        "user_id" -> rep.user.urn.getIdentifier.toLong,
        "duration" -> rep.track.duration,
        "commentable" -> rep.track.commentable,
        //"state" ->
        // original_content_size ->
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
        // original_format
        "license" -> rep.track.license,
        "uri" -> s"https://api.soundcloud.com/tracks/${rep.id}",
        "user" -> writeUser(rep.user),
        // user_favorite --> liebling
        "permalink_url" -> rep.track.permalink_url,
        // Probably we need to copy the logic at
        // https://github.com/soundcloud/api-web/blob/master/src/main/scala/com/soundcloud/api/web/representation/helpers/ResourceURLs.scala#L72
        "artwork_url" -> rep.track.artwork.filename.map(file => s"https://i1.sndcdn.com/$file"),
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
        // secret_token
        // secret_uri
        // attachments
        // attachments_uri
        // likes_count
        // reposts_count
        // available_country_codes
        // downloads_remaining
        // artwork_url
        // domain_lockings
        // user_favorite
        // user_playback_count
      )

      coreObject ++
        labelObjectFor(rep) ++
        geoblockingsObjectFor(rep)
    }
  }

  private def labelObjectFor(rep: TrackRepresentation): JsObject =
    rep.label match {
      case Some(user) => Json.obj("label" -> writeUser(user))
      case None => Json.obj()
    }

  private def geoblockingsObjectFor(rep: TrackRepresentation): JsObject =
    rep.geoblockings match {
      case Some(list) => Json.obj("available_country_codes" -> Country.officiallyAssignedAlpha2Codes.--(list))
      case None => Json.obj()
    }

  private def writeUser(user: User): JsValue =
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
