package com.soundcloud.publicApiStrangler.mapper.trackcoordinator

import com.soundcloud.trackcoordinator.client.representation.{Error, Errors, Failure, NotFound, Result, Success, Track => CoordinatorTrack, TrackUpdate}
import com.soundcloud.service.request.representation.MissingValue
import com.twitter.util.Try
import com.soundcloud.scalakit.json.{Json => ScJson}
import play.api.libs.json._

object TrackCoordinatorMapper {
  def trackUpdateFromPublicApiTrack(str: String)(coordinatorTrack: Result[CoordinatorTrack]): Option[TrackUpdate] =
    Try {
      val json = ScJson.fromString(str)
      new TrackUpdate(
        monetization = None,
        publisher_metadata = None,
        schedule = None,
        api_streamable = (json \ "streamable").asOpt[Boolean],
        commentable = (json \ "commentable").asOpt[Boolean],
        description = (json \ "description").asOpt[String],
        downloadable = (json \ "downloadable").asOpt[Boolean],
        embeddable = (json \ "embeddable").asOpt[Boolean],
        feedable = coordinatorTrack.asOption.flatMap(_.feedable),
        genre = (json \ "genre").asOpt[String],
        geo_blockings = coordinatorTrack.asOption.flatMap(_.geo_blockings),
        label_name = (json \ "label_name").asOpt[String],
        license = (json \ "license").asOpt[String],
        original_filename = None,
        permalink = (json \ "permalink").as[String],
        purchase_title = (json \ "purchase_title").asOpt[String],
        purchase_url = (json \ "purchase_url").asOpt[String],
        release_date = (json \ "release_date").asOpt[String],
        reveal_comments = coordinatorTrack.asOption.flatMap(_.reveal_comments),
        reveal_stats = coordinatorTrack.asOption.flatMap(_.reveal_stats),
        sharing = (json \ "sharing").asOpt[String],
        tag_list = (json \ "tag_list").asOpt[String],
        title = (json \ "title").as[String],
        replacing_uid = None,
        replacing_original_filename = None,
        artwork_from_s3 = MissingValue,
        desired_geo_policy_events = coordinatorTrack.asOption.flatMap(_.desired_geo_policy_events),
        restrictions = coordinatorTrack.asOption.flatMap(_.restrictions.map(_.map(_.getPrintName))),
        published_at = MissingValue
      )
    }.toOption

  def publicApiTrackFromCoordinatorTrack(track: CoordinatorTrack): Track = Track(
    id = track.urn.getIdentifier.toLong,
    created_at = track.created_at,
    user_id = track.user_urn.getIdentifier.toLong,
    title = track.title,
    permalink = track.permalink,
    permalink_url = track.permalink_url,
    uri = track.uri,
    sharing = track.sharing,
    embeddable_by = track.embeddable_by,
    purchase_url = track.purchase_url,
    artwork_url = track.artwork_url,
    description = track.description,
    duration = track.duration,
    genre = track.genre,
    tag_list = track.tag_list,
    label_id = track.label_id,
    label_name = track.label_name,
    release = None,
    release_day = track.release_day,
    release_month = track.release_month,
    release_year = track.release_year,
    streamable = track.streamable,
    downloadable = track.downloadable,
    state = track.state,
    license = track.license,
    track_type = track.track_type,
    waveform_url = track.waveform_url,
    download_url = track.download_url,
    stream_url = track.stream_url,
    video_url = None,
    bpm = None,
    commentable = track.commentable,
    isrc = track.isrc,
    key_signature = None,
    comment_count = track.comments_count,
    download_count = track.downloads_count,
    favoritings_count = track.favoritings_count,
    original_format = track.original_format,
    original_content_size = track.original_content_size,
    created_with = None,
    user_favourite = None
  )
}
