package com.soundcloud.publicApiStrangler.client.playlists

import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.scalakit.json.Json
import com.soundcloud.publicApiStrangler.test.UnitSpecification

class PlaylistSpec extends UnitSpecification {
  "reads from JSON" >> {

    val json =
      """
        |{
        |  "created_at": "2016/10/06 13:44:50 +0000",
        |  "description": null,
        |  "genre": null,
        |  "id": 265129730,
        |  "label_id": null,
        |  "label_name": null,
        |  "permalink": "private",
        |  "public": false,
        |  "purchase_url": null,
        |  "release": null,
        |  "release_date": "",
        |  "title": "Private",
        |  "self": {
        |    "urn": "soundcloud:sets:265129730",
        |    "url": "http://moshimoshi.int.s-cloud.net/playlists/soundcloud:sets:265129730"
        |  },
        |  "permalink_url": "https://soundcloud.com/hannes/sets/private",
        |  "track_count": 0,
        |  "user": {
        |    "urn": "soundcloud:users:123",
        |    "url": "http://moshimoshi.int.s-cloud.net/users/soundcloud:users:123"
        |  },
        |  "artwork_url": null,
        |  "original_artwork_url": null,
        |  "duration": 2742,
        |  "likes_count": 0,
        |  "reposts_count": 0,
        |  "sharing": "private",
        |  "streamable": true,
        |  "license": "all-rights-reserved",
        |  "tag_list": "",
        |  "embeddable_by": "all",
        |  "purchase_title": null,
        |  "secret_token": "s-YZzGa",
        |  "release_year": null,
        |  "release_month": null,
        |  "release_day": null,
        |  "last_modified": "2017/01/03 13:49:44 +0000",
        |  "set_type": "",
        |  "is_album": false,
        |  "published_at": null,
        |  "content_ingestion_status": "not_ingested",
        |  "ean": null
        |}
      """.stripMargin

    val playlist = Json.fromString(json).as[Playlist]

    playlist.userUrn ==== Urn("soundcloud:users:123")
    playlist.secretToken ==== "s-YZzGa"
  }
}
