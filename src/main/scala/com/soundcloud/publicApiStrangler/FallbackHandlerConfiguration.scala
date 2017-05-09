package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.App.mothershipDispatcher
import com.soundcloud.publicApiStrangler.support.SpecificStranglingHandler

trait FallbackHandlerConfiguration {

  def moduleTelemetry: Telemetry

  private val officialSoundCloudApps = List(
    Urn("soundcloud:applications:46941"), // SoundCloud.com (currently being abused) Internal
    Urn("soundcloud:applications:124"), // SoundCloud iOS Internal
    Urn("soundcloud:applications:3152"), // SoundCloud Android Internal
    Urn("soundcloud:applications:3273"), // Mobile Soundcloud Internal
    Urn("soundcloud:applications:65097"), // Mobi (new mobile soundcloud) Internal
    Urn("soundcloud:applications:-1"), // Classic Internal
    Urn("soundcloud:applications:43164"), // SoundCloud Player Widget Internal
    Urn("soundcloud:applications:90575"), // SoundCloud Visual Embed Player Internal
    Urn("soundcloud:applications:60973"), // SoundCloud Flash Widget Internal
    Urn("soundcloud:applications:66151"), // Old mobi web Internal
    Urn("soundcloud:applications:3537"), // SoundCloud Desktop Internal
    Urn("soundcloud:applications:99561"), // SoundCloud Kik Messenger Card Internal
    Urn("soundcloud:applications:120502"), // Twitter Partner Internal
    Urn("soundcloud:applications:42975"), // SoundCloud Notifications Internal
    Urn("soundcloud:applications:147241"), // SoundCloud Jobs Page Internal
    Urn("soundcloud:applications:140141"), // SoundCloud Chromecast Receiver Internal
    Urn("soundcloud:applications:179522") // Facebook Partner Internal
  )

  private val whatToStrangle = {
    // Endpoints we officially support: https://developers.soundcloud.com/docs/api/reference
    val officiallySupported = List(
      """/connect""",
      """/oauth2/token""",
      """/users/\d+""",
      """/tracks/\d+""",
      """/playlists/\d+""",
      """/comments/\d+""",
      """/me""",
      """/me/connections""",
      """/me/connections/\d+""",
      """/apps""",
      """/resolve""",
      """/oembed"""
    )

    // Newly discovered endpoints:
    val newlyDiscovered = List(
      """/announcements""",
      """/search/sounds""",
      """/search/sets""",
      """/e1/playlists/\d+/domain-lockings""",
      """/e1/shorten""",
      """/i1/comments/\d+/spam""",
      """/transcodings/.*""",
      """/me/track_likes/ids""",
      """/me/playlist_likes/ids""",
      """/me/shortcuts""",
      """/me/favorites""",
      """/me/tracks""",
      """/me/track_reposts/ids""",
      """/me/playlist_reposts/ids""",
      """/me/stats""",
      """/tracks/\d+/download""",
      """/tracks/\d+/comments""",
      """/tracks/\d+/stream""",
      """/tracks/\d+/streams""",
      """/tracks/\d+/related""",
      """/tracks/\d+/groups""",
      """/i1/tracks/\d+/streams""",
      """/tracks/[a-zA-Z0-9\-\_]+""",
      """/tracks/[a-zA-Z0-9\-\_]+/download""",
      """/tracks/[a-zA-Z0-9\-\_]+/comments""",
      """/tracks/[a-zA-Z0-9\-\_]+/stream""",
      """/tracks/[a-zA-Z0-9\-\_]+/streams""",
      """/tracks/[a-zA-Z0-9\-\_]+/related""",
      """/i1/tracks/[a-zA-Z0-9\-\_]+/streams""",
      """/playlists/\d+""",
      """/playlists/\d+/tracks""",
      """/playlists/[a-zA-Z0-9\-\_]+""",
      """/upload/policy""",
      """/users""",
      """/users/\d+/groups""",
      """/users/\d+/favorites""",
      """/users/\d+/tracks""",
      """/users/\d+/comments""",
      """/users/\d+/playlists""",
      """/users/\d+/web-profiles""",
      """/users/[a-zA-Z0-9\_\-]+""",
      """/users/[a-zA-Z0-9\_\-]+/groups""",
      """/users/[a-zA-Z0-9\-\_]+/favorites""",
      """/users/[a-zA-Z0-9\-\_]+/tracks""",
      """/users/[a-zA-Z0-9\-\_]+/comments""",
      """/users/[a-zA-Z0-9\-\_]+/playlists""",
      """/users/[a-zA-Z0-9\-\_]+/playlists/\d+""",
      """/users/[a-zA-Z0-9\-\_]+/web-profiles""",
      """/users/\d+/followings/not_followed_by/""",
      """/e1/users/\d+/sounds""",
      """/e1/users/\d+/likes""",

      // https://github.com/soundcloud/soundcloud/blob/master/config/routes.rb#L228-L238
      """/e1/me/likes""",
      """/e1/me/sounds""",
      """/e1/me/reposts""",
      """/e1/me/track_likes""",
      """/e1/me/track_reposts""",
      """/e1/me/playlist_likes""",
      """/e1/me/playlist_reposts""",
      """/e1/me/track_likes/ids""",
      """/e1/me/track_reposts/ids""",
      """/e1/me/playlist_likes/ids""",
      """/e1/me/playlist_reposts/ids""",

      // https://github.com/soundcloud/soundcloud/blob/master/config/routes.rb#L240-L246
      """/i1/me/shortcuts"""
    )

    // Everything else, to be compatible with what we have right now
    val unknown = List(".*".r)

    val everything = officiallySupported ++ newlyDiscovered

    everything.flatMap { endpoint =>
      // Wrap regex with start and end anchors. May have .json at the end. May have trailing slash.
      val patternWithOptionalJsonAndSlash = endpoint + "(\\.json)?/?"
      val originalPattern = ("^" + patternWithOptionalJsonAndSlash + "$").r
      val v1Pattern = ("^/v1" + patternWithOptionalJsonAndSlash + "$").r

      // All endpoints may be prefixed with v1 - record these separately
      List(originalPattern, v1Pattern)
    } ++ unknown
  }

  private val fallthroughCounter = moduleTelemetry.counter(
    "fallthrough_strangled_by",
    "Fallthrough requests by the path pattern that strangles them",
    "method",
    "path_pattern",
    "agent_urn"
  )

  val fallbackHandler =
    new SpecificStranglingHandler(mothershipDispatcher,
      whatToStrangle,
      officialSoundCloudApps,
      fallthroughCounter)

}
