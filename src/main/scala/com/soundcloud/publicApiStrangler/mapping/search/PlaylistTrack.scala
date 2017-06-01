package com.soundcloud.publicApiStrangler.mapping.search

import com.fasterxml.jackson.annotation.JsonIgnore
import com.soundcloud.publicApiStrangler.representation.Track
import com.soundcloud.publicApiStrangler.support.mapping.ObjectMapping


trait PlaylistTracks extends ObjectMapping[List[Track]] {
  self =>
  def baseUrl: String

  val tracks = resource.map(t => new ObjectMapping(t) with PlaylistTrack {
    @JsonIgnore override def baseUrl: String = self.baseUrl
  })
}

/**
  * essentially copy-paste from TrackSummary, but
  * maps a Track instead of a JsValue
  */
trait PlaylistTrack extends ObjectMapping[Track] {
  @JsonIgnore def baseUrl: String

  val id = resource.urn.getIdentifier.toInt
  val kind = "track"
  val created_at = resource.created_at
  val last_modified = resource.last_modified
  val permalink = resource.permalink
  val permalink_url = resource.permalink_url
  val title = resource.title
  val sharing = resource.sharing
  val duration = resource.duration
  val waveform_url = resource.waveform_url
  // use media-service?
  val stream_url = resource.stream_url
  val uri = resource.uri
  val user_id = resource.user_urn.getIdentifier.toInt
  val user_uri = s"$baseUrl/users/$user_id"
}
