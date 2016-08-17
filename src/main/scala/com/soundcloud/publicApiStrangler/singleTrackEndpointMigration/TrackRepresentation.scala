package com.soundcloud.publicApiStrangler.singleTrackEndpointMigration

import com.soundcloud.jvmkit.Urn
import com.soundcloud.publicApiStrangler.singleTrackEndpointMigration.trackmetadataclient.Track
import play.api.libs.json._

/*
The following fields are not yet included in the track representation. Add them, and remove them from this list:

"state": "finished",
"original_content_size": 78361171,
"last_modified": "2013/12/13 20:34:14 +0000",
"sharing": "public",
"permalink": "business-mix",
"streamable": true,
"downloadable": true,
"purchase_url": null,
"label_id": null,
"purchase_title": null,
"genre": "house",
"title": "Business Mix",
"description": "",
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
  user_urn: Urn
)

object TrackRepresentation {
  implicit val writes = new Writes[TrackRepresentation] {
    override def writes(rep: TrackRepresentation): JsValue = Json.obj(
      "kind" -> "track",
      "id" -> rep.track.urn.getIdentifier.toLong,
      "user_id" -> rep.user_urn.getIdentifier.toLong,
      "duration" -> rep.track.duration,
      "created_at" -> rep.track.created_at.toString,
      "commentable" -> rep.track.commentable,
      "embeddable_by" -> rep.track.embeddableBy,
      "tag_list" -> (rep.track.user_tags ++ rep.track.machine_tags).mkString(", "),
      "artwork_url" -> rep.track.artwork.filename
    )
  }
}
