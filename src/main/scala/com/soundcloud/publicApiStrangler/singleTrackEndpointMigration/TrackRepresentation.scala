package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.client.pubmese.Isrc
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import com.soundcloud.service.response.representation.User
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._

case class TrackRepresentation(track: Track, user: User, isrc: Option[Isrc]) {
  def id = track.urn.getIdentifier.toLong
}

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")

  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue =
      Json.obj(
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
        // "purchase_url" --> track metadata
        // "label_id" ->
        // "purchase_title" --> track metadata
        "genre" -> rep.track.genre,
        "title" -> rep.track.title,
        "description" -> rep.track.description,
        "label_name" -> rep.track.label_name,
        // "release" --> track metadata
        // track_type --> track metadata
        // key_signature --> track metadata
        "isrc" -> rep.isrc.map(_.toString),
        // video_url --> track metadata
        // bpm --> track metadata
        "release_year" -> rep.track.release_year,
        "release_month" -> rep.track.release_month,
        "release_day" -> rep.track.release_day,
        // original_format
        "license" -> rep.track.license,
        "uri" -> s"https://api.soundcloud.com/tracks/${rep.id}",
        "user" -> writeUser(rep),
        // user_favorite --> liebling
        "permalink_url" -> rep.track.permalink_url,
        // Probably we need to copy the logic at
        // https://github.com/soundcloud/api-web/blob/master/src/main/scala/com/soundcloud/api/web/representation/helpers/ResourceURLs.scala#L72
        "artwork_url" -> rep.track.artwork.filename.map(file => s"https://i1.sndcdn.com/$file"),
        // waveform_url --> media service
        "stream_url" -> s"https://api.soundcloud.com/tracks/${rep.id}/stream",
        // playback_count --> stitch
        "download_url" -> s"https://api.soundcloud.com/tracks/${rep.id}/download"
        // download_count --> stitch
        // favoritings_count --> stitch
        // comment_count --> stitch
        // likes_count --> stitch
        // reposts_count --> stitch
      )
  }

  private def writeUser(rep: TrackRepresentation): JsValueWrapper = {
    Json.obj(
      "id" -> rep.user.urn.getIdentifier.toLong,
      "kind" -> "user",
      "permalink" -> rep.user.permalink,
      "username" -> rep.user.username,
      // last_modified
      "uri" -> s"https://api.soundcloud.com/users/${rep.user.urn.getIdentifier}",
      "permalink_url" -> rep.user.permalink_url,
      "avatar_url" -> rep.user.avatar_url
    )
  }
}
