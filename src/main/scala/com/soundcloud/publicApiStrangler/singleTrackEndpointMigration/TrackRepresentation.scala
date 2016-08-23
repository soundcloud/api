package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

/*
The following fields are not yet included in the track representation. Add them, and remove them from this list:

"label_name": null,
"release": null,
"track_type": null,
"key_signature": null,
"isrc": null,
"video_url": null,
"bpm": null,
"release_year": null,
"release_month": null,
"release_day": null,
"original_format": "mp3",
"license": "all-rights-reserved",
"uri": "https://api.soundcloud.com/tracks/124707269",
"permalink_url": "http://soundcloud.com/cal_green/business-mix",
"waveform_url": "https://w1.sndcdn.com/63Vb39yH93WZ_m.png",
"stream_url": "https://api.soundcloud.com/tracks/124707269/stream",
"download_url": "https://api.soundcloud.com/tracks/124707269/download",
"playback_count": 56,
"download_count": 17,
"favoritings_count": 4,
"comment_count": 2,
"attachments_uri": "https://api.soundcloud.com/tracks/124707269/attachments"
*/

case class TrackRepresentation(
                                track: Track,
                                userUrn: Urn
                              )

object TrackRepresentation {
  private val dateTimeFormat = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss +0000")

  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue =
      Json.obj(
        "kind" -> "track",
        "id" -> rep.track.urn.getIdentifier.toLong,
        "created_at" -> rep.track.created_at.toString(dateTimeFormat),
        "user_id" -> rep.userUrn.getIdentifier.toLong,
        "duration" -> rep.track.duration,
        "commentable" -> rep.track.commentable,
        //"state" ->
        // original_content_size ->
        "last_modified" -> rep.track.last_modified.toString(dateTimeFormat),
        "sharing" -> (if (rep.track.public) "public" else "private"),
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
        "tag_list" -> (rep.track.user_tags ++ rep.track.machine_tags).mkString(", "),
        "artwork_url" -> rep.track.artwork.filename
      )
  }
}
