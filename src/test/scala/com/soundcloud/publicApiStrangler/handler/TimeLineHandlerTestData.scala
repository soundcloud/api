package com.soundcloud.publicApiStrangler.handler

import com.soundcloud.jvmkit.module.util.Urn
import play.api.libs.json.{JsNull, Json}

trait TimeLineHandlerTestData {

  val usrUrn = Urn("soundcloud:users:1")
  val trackUrn = Urn("soundcloud:tracks:2")
  val playlistUrn = Urn("soundcloud:playlists:3")
  val baseUrl = "www.soundcloud.com"

  val userJson = Json.obj(
    "urn" -> usrUrn.toString(),
    "self" -> Json.obj("urn" -> "soundcloud:users:1"),
    "links" -> Json.arr(Json.obj("network" -> "facebook")),
    "avatar_url" -> "avatar:url",
    "id" -> "123",
    "kind" -> "kind",
    "permalink_url" -> "some/permalink/url",
    "uri" -> "userUri",
    "username" -> "JohnD",
    "permalink" -> "some/permalink",
    "last_modified" -> "2017-04-01",
    "first_name" -> "John",
    "last_name" -> "Doe",
    "full_name" -> "John Doe",
    "city" -> "London",
    "description" -> "Hottest guy in town",
    "country" -> "UK",
    "track_count" -> 28,
    "public_favorites_count" -> 12,
    "followers_count" -> 42,
    "followings_count" -> 23,
    "plan" -> "go+",
    "myspace_name" -> "JohnDoeMyspace",
    "discogs_name" -> "JohnDowDiscogs",
    "website_title" -> "John Doe's page",
    "website" -> "www.therealjohndoe.com",
    "reposts_count" -> 33,
    "comments_count" -> 5,
    "online" -> false,
    "likes_count" -> 1234,
    "playlist_count" -> 2)

  val testTrackJson = Json.obj(
    "urn" -> trackUrn.toString(),
    "self" -> Json.obj("urn" -> trackUrn.toString()),
    "timestamp" -> "2017-01-01",
    "artwork_url" -> "some/artwork",
    "comments_count" -> 5,
    "commentable" -> true,
    "description" -> "description",
    "downloads_count" -> 1,
    "downloadable" -> true,
    "has_downloads_left" -> false,
    "embeddable_by" -> "embeddable_by",
    "favoritings_count" -> 42,
    "genre" -> "genre",
    "isrc" -> "isrc",
    "label_id" -> 23,
    "label_name" -> "label_name",
    "license" -> "license",
    "original_content_size" -> 123,
    "original_format" -> "original_format",
    "playback_count" -> 1,
    "purchase_title" -> "purchase_title",
    "purchase_url" -> "purchase_url",
    "release" -> "release",
    "release_day" -> 1,
    "release_month" -> 1,
    "release_year" -> 1970,
    "reposts_count" -> 123,
    "state" -> "state",
    "streamable" -> true,
    "tag_list" -> "tag_list",
    "track_type" -> "track_type",
    "user" -> userJson
  )


  val playlistJson = Json.obj(
    "urn" -> playlistUrn.toString(),
    "self" -> Json.obj("urn" -> playlistUrn.toString()),
    "created_at" -> "created_at",
    "duration" -> "duration",
    "last_modified" -> "last_modified",
    "sharing" -> "sharing",
    "tag_list" -> "tag_list",
    "permalink" -> "permalink",
    "track_count" -> 20,
    "streamable" -> true,
    "embeddable_by" -> "embeddable_by",
    "description" -> "description",
    "genre" -> "genre",
    "release" -> "release",
    "label_name" -> "label_name",
    "title" -> "title",
    "release_year" -> "2017",
    "release_month" -> "4",
    "release_day" -> "4",
    "permalink_url" -> "permalink_url",
    "artwork_url" -> "artwork_url",
    "license" -> "license",
    "secret_token" -> "secret_token"
  )


  val timeline = Json.obj("events" -> Json.arr(
    Json.obj(
      "type" -> "track",
      "timestamp" -> "2014/08/12 06:09:34 +0000",
      "urn" -> trackUrn.toString(),
      "actor" -> usrUrn.toString(),
      "target" -> JsNull
    )
    ,
    Json.obj(
      "type" -> "playlist",
      "timestamp" -> "2014/08/12 01:16:44 +0000",
      "urn" -> playlistUrn.toString(),
      "actor" -> usrUrn.toString(),
      "target" -> JsNull
    )
  ))

  val onlyTracksTimeline = Json.obj("events" -> Json.arr(
    Json.obj(
      "type" -> "track",
      "timestamp" -> "2014/08/12 06:09:34 +0000",
      "urn" -> trackUrn.toString(),
      "actor" -> usrUrn.toString(),
      "target" -> JsNull
    )
  ))

  /**
    * The endpoint is encoded in the response, so keep this part variable
    */
  def timelineJsonString(endpoint: String) =
    s"""{"collection":[{"track":{"id":2,"kind":"track","created_at":null,"last_modified":null,"permalink":null,"permalink_url":null,"title":null,"duration":null,"sharing":null,"waveform_url":null,"stream_url":"www.soundcloud.com/tracks/2/stream","uri":"www.soundcloud.com/tracks/2","user_id":1,"user_uri":"www.soundcloud.com/users/1","artwork_url":"some/artwork","comment_count":5,"commentable":true,"description":"description","download_count":1,"downloadable":false,"embeddable_by":"embeddable_by","favoritings_count":42,"genre":"genre","isrc":"isrc","label_id":23,"label_name":"label_name","license":"license","original_content_size":123,"original_format":"original_format","playback_count":1,"purchase_title":"purchase_title","purchase_url":"purchase_url","release":"release","release_day":1,"release_month":1,"release_year":1970,"reposts_count":2345,"state":"state","streamable":true,"tag_list":"tag_list","track_type":"track_type","user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"likes_count":1234,"attachments_uri":null,"bpm":null,"key_signature":null,"user_favorite":null,"user_playback_count":null,"video_url":null},"user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"uuid":null,"tags":null,"created_at":"2014/08/12 06:09:34 +0000","type":"track"},{"playlist":{"kind":"playlist","id":3,"created_at":"created_at","duration":null,"last_modified":"last_modified","sharing":"sharing","tag_list":"tag_list","permalink":"permalink","track_count":20,"streamable":true,"embeddable_by":"embeddable_by","description":"description","genre":"genre","release":"release","label_name":"label_name","title":"title","release_year":"2017","release_month":"4","release_day":"4","uri":"www.soundcloud.com/playlists/3","permalink_url":"permalink_url","artwork_url":"artwork_url","license":"license","user_id":null,"user":null,"secret_token":"secret_token","reposts_count":84,"tracks_uri":"www.soundcloud.com/playlists/3/tracks","secret_uri":"www.soundcloud.com/playlists/3?secret_token=secret_token","likes_count":34,"downloadable":null,"type":null,"purchase_url":null,"playlist_type":null,"ean":null,"purchase_title":null,"created_with":null},"user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"uuid":null,"tags":null,"created_at":"2014/08/12 01:16:44 +0000","type":"playlist"}],"next_href":null,"future_href":"www.soundcloud.com${stripTrailingSlash(endpoint)}?limit=10"}"""

  def publicCompleteTimelineJsonString(endpoint: String) =
    s"""{"collection":[{"origin":{"id":2,"kind":"track","created_at":null,"last_modified":null,"permalink":null,"permalink_url":null,"title":null,"duration":null,"sharing":null,"waveform_url":null,"stream_url":"www.soundcloud.com/tracks/2/stream","uri":"www.soundcloud.com/tracks/2","user_id":1,"user_uri":"www.soundcloud.com/users/1","artwork_url":"some/artwork","comment_count":5,"commentable":true,"description":"description","download_count":1,"downloadable":false,"embeddable_by":"embeddable_by","favoritings_count":42,"genre":"genre","isrc":"isrc","label_id":23,"label_name":"label_name","license":"license","original_content_size":123,"original_format":"original_format","playback_count":1,"purchase_title":"purchase_title","purchase_url":"purchase_url","release":"release","release_day":1,"release_month":1,"release_year":1970,"reposts_count":2345,"state":"state","streamable":true,"tag_list":"tag_list","track_type":"track_type","user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"likes_count":1234,"attachments_uri":null,"bpm":null,"key_signature":null,"user_favorite":null,"user_playback_count":null,"video_url":null},"tags":null,"created_at":"2014/08/12 06:09:34 +0000","type":"track"},{"origin":{"kind":"playlist","id":3,"created_at":"created_at","duration":null,"last_modified":"last_modified","sharing":"sharing","tag_list":"tag_list","permalink":"permalink","track_count":20,"streamable":true,"embeddable_by":"embeddable_by","description":"description","genre":"genre","release":"release","label_name":"label_name","title":"title","release_year":"2017","release_month":"4","release_day":"4","uri":"www.soundcloud.com/playlists/3","permalink_url":"permalink_url","artwork_url":"artwork_url","license":"license","user_id":null,"user":null,"secret_token":"secret_token","reposts_count":84,"tracks_uri":"www.soundcloud.com/playlists/3/tracks","secret_uri":"www.soundcloud.com/playlists/3?secret_token=secret_token","likes_count":34,"downloadable":null,"type":null,"purchase_url":null,"playlist_type":null,"ean":null,"purchase_title":null,"created_with":null},"tags":null,"created_at":"2014/08/12 01:16:44 +0000","type":"playlist"}],"next_href":null,"future_href":"www.soundcloud.com${stripTrailingSlash(endpoint)}?limit=10"}"""

  def streamTimelineJsonString(endpoint: String) =
    s"""{"collection":[{"track":{"id":2,"kind":"track","created_at":null,"last_modified":null,"permalink":null,"permalink_url":null,"title":null,"duration":null,"sharing":null,"waveform_url":null,"stream_url":"www.soundcloud.com/tracks/2/stream","uri":"www.soundcloud.com/tracks/2","user_id":1,"user_uri":"www.soundcloud.com/users/1","artwork_url":"some/artwork","comment_count":5,"commentable":true,"description":"description","download_count":1,"downloadable":false,"embeddable_by":"embeddable_by","favoritings_count":42,"genre":"genre","isrc":"isrc","label_id":23,"label_name":"label_name","license":"license","original_content_size":123,"original_format":"original_format","playback_count":1,"purchase_title":"purchase_title","purchase_url":"purchase_url","release":"release","release_day":1,"release_month":1,"release_year":1970,"reposts_count":2345,"state":"state","streamable":true,"tag_list":"tag_list","track_type":"track_type","user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"likes_count":1234,"attachments_uri":null,"bpm":null,"key_signature":null,"user_favorite":null,"user_playback_count":null,"video_url":null},"user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"uuid":null,"tags":null,"created_at":"2014/08/12 06:09:34 +0000","type":"track"},{"playlist":{"kind":"playlist","id":3,"created_at":"created_at","duration":null,"last_modified":"last_modified","sharing":"sharing","tag_list":"tag_list","permalink":"permalink","track_count":20,"streamable":true,"embeddable_by":"embeddable_by","description":"description","genre":"genre","release":"release","label_name":"label_name","title":"title","release_year":"2017","release_month":"4","release_day":"4","uri":"www.soundcloud.com/playlists/3","permalink_url":"permalink_url","artwork_url":"artwork_url","license":"license","user_id":null,"user":null,"secret_token":"secret_token","reposts_count":84,"tracks_uri":"www.soundcloud.com/playlists/3/tracks","secret_uri":"www.soundcloud.com/playlists/3?secret_token=secret_token","likes_count":34,"downloadable":null,"type":null,"purchase_url":null,"playlist_type":null,"ean":null,"purchase_title":null,"created_with":null},"user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"uuid":null,"tags":null,"created_at":"2014/08/12 01:16:44 +0000","type":"playlist"}],"next_href":null,"future_href":"www.soundcloud.com${stripTrailingSlash(endpoint)}?limit=10"}"""

  def tracksOnlyTimelineJsonString() =
    s"""[{"id":2,"kind":"track","created_at":null,"last_modified":null,"permalink":null,"permalink_url":null,"title":null,"duration":null,"sharing":null,"waveform_url":null,"stream_url":"www.soundcloud.com/tracks/2/stream","uri":"www.soundcloud.com/tracks/2","user_id":1,"user_uri":"www.soundcloud.com/users/1","artwork_url":"some/artwork","comment_count":5,"commentable":true,"description":"description","download_count":1,"downloadable":false,"embeddable_by":"embeddable_by","favoritings_count":42,"genre":"genre","isrc":"isrc","label_id":23,"label_name":"label_name","license":"license","original_content_size":123,"original_format":"original_format","playback_count":1,"purchase_title":"purchase_title","purchase_url":"purchase_url","release":"release","release_day":1,"release_month":1,"release_year":1970,"reposts_count":2345,"state":"state","streamable":true,"tag_list":"tag_list","track_type":"track_type","user":{"avatar_url":"avatar:url","id":1,"kind":"user","permalink_url":"some/permalink/url","uri":"www.soundcloud.com/users/1","username":"JohnD","permalink":"some/permalink","last_modified":"2017-04-01","first_name":"John","last_name":"Doe","full_name":"John Doe","city":"London","description":"Hottest guy in town","country":"UK","track_count":null,"public_favorites_count":12,"followers_count":42,"followings_count":23,"plan":"go+","myspace_name":null,"discogs_name":null,"website_title":null,"website":null,"reposts_count":33,"comments_count":5,"online":false,"likes_count":12,"playlist_count":null},"likes_count":1234,"attachments_uri":null,"bpm":null,"key_signature":null,"user_favorite":null,"user_playback_count":null,"video_url":null}]"""

  def stripTrailingSlash(s: String): String = s.charAt(s.size - 1) match {
    case '/' => s.substring(0, s.size - 1)
    case _ => s
  }

}
