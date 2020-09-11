package com.soundcloud.publicApiStrangler.handler.support.requestParser

import com.soundcloud.publicApiStrangler.client.mothership.request.representation.{NonNullValue, Value}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.soundcloud.publicApiStrangler.test.fixtures.Fixtures._
import play.api.libs.json.Json

class TrackMetadataUpdateRequestSpec extends UnitSpecification {
  trait Context extends Scope {
    val metadataUpdatesExpected = TrackMetadataUpdateRequest(
      track = TrackMetadataUpdates(
        api_streamable = Value(true),
        commentable = Value(true),
        description = Value("a random description"),
        downloadable = Value(false),
        embeddable = Value(true),
        genre = Value("bossa nova"),
        geo_blockings = Value(List("EN", "DE")),
        isrc = Value("is-rc"),
        label_name = Value("alexxx"),
        license = Value("test_license"),
        permalink = NonNullValue("permalinky"),
        purchase_url = Value("purchase.com/track"),
        release_date = Value("2008/04/29 22:14:25 +0000"),
        sharing = Value("all"),
        tag_list = Value("tags, tags, tags"),
        title = NonNullValue("new title"),
        reveal_stats = Value(false),
        reveal_comments = Value(true),
        purchase_title = Value("new title")
      )
    )

    val requestBodyExpected =
      """{"title":"new title","permalink":"permalinky","api_streamable":true,"commentable":true,"description":"a random description","downloadable":false,"embeddable":true,"genre":"bossa nova","geo_blockings":["EN","DE"],"license":"test_license","purchase_title":"new title","release_date":"2008/04/29 22:14:25 +0000","reveal_comments":true,"reveal_stats":false,"tag_list":"tags, tags, tags","purchase_url":"purchase.com/track","sharing":"all","label_name":"alexxx","publisher_metadata":{"isrc":"is-rc"}}"""
  }

  "can read json" in new Context {
    val metadataUpdates = updateTrackJson.as[TrackMetadataUpdateRequest]
    metadataUpdates ==== metadataUpdatesExpected
  }

  "can read from map" in new Context {
    val inputArgMap = Map(
      "streamable" -> "true",
      "commentable" -> "true",
      "description" -> "a random description",
      "downloadable" -> "false",
      "embeddable_by" -> "all",
      "genre" -> "bossa nova",
      "geo_blockings" -> "EN,DE",
      "isrc" -> "is-rc",
      "label_name" -> "alexxx",
      "license" -> "test_license",
      "permalink" -> "permalinky",
      "purchase_url" -> "purchase.com/track",
      "release_date" -> "2008/04/29 22:14:25 +0000",
      "sharing" -> "all",
      "tag_list" -> "tags, tags, tags",
      "title" -> "new title",
      "reveal_stats" -> "false",
      "reveal_comments" -> "true",
      "purchase_title" -> "new title"
    )

    val metadataUpdates = TrackMetadataUpdateRequest.fromForm(inputArgMap)
    metadataUpdates.isEmpty ==== false
    metadataUpdates.get ==== metadataUpdatesExpected
  }

  "can write to request body" in new Context {
    val metadataUpdates = updateTrackJson.as[TrackMetadataUpdateRequest]
    val requestBody = Json.stringify(Json.toJson(metadataUpdates.track))
    requestBody ==== requestBodyExpected
  }
}
