package com.soundcloud.apipublic.client.mothership.request.representation

import com.soundcloud.apipublic.test.UnitSpecification
import play.api.libs.json.{JsNull, Json}

class PlaylistUpdateSpec extends UnitSpecification {
  "PlaylistUpdate params renders all specified keys" >> {
    val playlistUpdate = PlaylistUpdate(
      title = Value("Some title"),
      public = Value(false),
      description = Value("Some description"),
      genre = Value("Rock"),
      tag_list = Value("Electronic, Jazz"),
      license = Value("cc-by-nc-sa"),
      label_name = Value("Some label"),
      release = Value("newCatalogueNumber"),
      release_date = Value("2015-03-02"),
      purchase_url = Value("http://soundcloud.com"),
      purchase_title = Value("Buy link"),
      ean = Value("0123456789012"),
      permalink = Value("permalink")
    )

    val playlistUpdateJson = Json.obj(
      "title" -> "Some title",
      "public" -> false,
      "description" -> "Some description",
      "genre" -> "Rock",
      "tag_list" -> "Electronic, Jazz",
      "license" -> "cc-by-nc-sa",
      "label_name" -> "Some label",
      "release" -> "newCatalogueNumber",
      "release_date" -> "2015-03-02",
      "purchase_url" -> "http://soundcloud.com",
      "purchase_title" -> "Buy link",
      "ean" -> "0123456789012",
      "permalink" -> "permalink"
    )

    PlaylistUpdate.writes.writes(playlistUpdate) ==== playlistUpdateJson
  }

  "PlaylistUpdate respects null and undefined fields" >> {
    val playlistUpdate = PlaylistUpdate(
      title = Value("Some title"),
      public = MissingValue,
      description = NullValue
    )

    val playlistUpdateJson = Json.obj(
      "title" -> "Some title",
      "description" -> JsNull
    )

    PlaylistUpdate.writes.writes(playlistUpdate) ==== playlistUpdateJson
  }

  "PlaylistUpdate params does not render unspecified keys" >> {
    val playlistUpdate = PlaylistUpdate()

    PlaylistUpdate.writes.writes(playlistUpdate) ==== Json.obj()
  }
}
