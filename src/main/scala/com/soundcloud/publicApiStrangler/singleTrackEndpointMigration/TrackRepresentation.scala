package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import com.soundcloud.service.response.representation.User
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

case class TrackRepresentation(track: Track, user: User)

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")

  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue =
      Json.obj(
        "kind" -> "track",
        "id" -> rep.track.urn.getIdentifier.toLong,
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
        // "purchase_url" ->
        // "label_id" ->
        // "purchase_title" ->
        "genre" -> rep.track.genre,
        "title" -> rep.track.title,
        "description" -> rep.track.description,
        "label_name" -> rep.track.label_name,
        // "release" ->
        // track_type
        // key_signature
        // isrc
        // video_url
        // bpm
        "release_year" -> rep.track.release_year,
        "release_month" -> rep.track.release_month,
        "release_day" -> rep.track.release_day,
        // original_format
        "license" -> rep.track.license,
        "uri" -> s"https://api.soundcloud.com/tracks/${rep.track.urn.getIdentifier}",
        "user" -> Json.obj(
          "id" -> rep.user.urn.getIdentifier
        ),
        // user_playback_count
        // user_favorite
        "permalink_url" -> rep.track.permalink_url,
        // Probably we need to copy the logic at
        // https://github.com/soundcloud/api-web/blob/master/src/main/scala/com/soundcloud/api/web/representation/helpers/ResourceURLs.scala#L72
        "artwork_url" -> rep.track.artwork.filename.map(file => s"https://i1.sndcdn.com/$file")
        // waveform_url
        // stream_url
        // playback_count
        // download_url
        // download_count
        // favoritings_count
        // comment_count
        // likes_count
        // reposts_count
        // attachments_uri
        // policy
        // monetization_model
      )
  }
}
