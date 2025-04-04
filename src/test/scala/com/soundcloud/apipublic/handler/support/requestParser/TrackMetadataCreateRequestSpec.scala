package com.soundcloud.apipublic.handler.support.requestParser

import com.soundcloud.apipublic.client.mothership.request.representation.{MissingValue, NonNullValue, Value}
import com.soundcloud.apipublic.test.UnitSpecification
import com.soundcloud.apipublic.test.fixtures.Fixtures._
import com.soundcloud.jvmkit.module.outcome._
import play.api.libs.json.{JsObject, Json}

class TrackMetadataCreateRequestSpec extends UnitSpecification {
  trait Context extends Scope {
    val createMetadataExpected = TrackMetadataCreateRequest(
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
        release = Value("1234"),
        release_date = Value("2008/04/29 22:14:25 +0000"),
        sharing = Value("all"),
        tag_list = Value("tags, tags, tags"),
        title = NonNullValue("new title"),
        reveal_stats = Value(false),
        reveal_comments = Value(true),
        purchase_title = Value("new title"),
        artist = MissingValue
      )
    )

    val requestBodyExpected =
      """{"title":"new title","permalink":"permalinky","api_streamable":true,"commentable":true,"description":"a random description","downloadable":false,"embeddable":true,"genre":"bossa nova","geo_blockings":["EN","DE"],"license":"test_license","purchase_title":"new title","release_date":"2008/04/29 22:14:25 +0000","reveal_comments":true,"reveal_stats":false,"tag_list":"tags, tags, tags","purchase_url":"purchase.com/track","sharing":"all","label_name":"alexxx","release":"1234","publisher_metadata":{"isrc":"is-rc","artist":"new artist"}}"""
  }

  "can read json" in new Context {
    val createMetadata = createTrackJson.as[TrackMetadataCreateRequest]
    createMetadata ==== createMetadataExpected
  }

  "does not parse if no title provided" in new Context {
    val trackData = (createTrackJson \ "track").get.as[JsObject]
    val trackDataWithoutTitle = trackData - "title"
    val createTrackJsonModified = Json.obj("track" -> trackDataWithoutTitle)

    val createMetadata = createTrackJsonModified.asOpt[TrackMetadataCreateRequest]
    createMetadata must beEmpty
  }

  "when reading from a Map" >> {
    "succeeds" in new Context {
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
        "release" -> "1234",
        "release_date" -> "2008/04/29 22:14:25 +0000",
        "sharing" -> "all",
        "tag_list" -> "tags, tags, tags",
        "title" -> "new title",
        "reveal_stats" -> "false",
        "reveal_comments" -> "true",
        "purchase_title" -> "new title"
      )

      val createMetadata = TrackMetadataCreateRequest.fromForm(inputArgMap)
      createMetadata ==== createMetadataExpected.good
    }

    trait BooleanTestContext extends Context {
      def inputMap(booleanString: String): Map[String, String] = {
        Map(
          "streamable" -> booleanString,
          "commentable" -> booleanString,
          "description" -> "a random description",
          "downloadable" -> booleanString,
          "embeddable_by" -> "all",
          "genre" -> "bossa nova",
          "geo_blockings" -> "EN,DE",
          "isrc" -> "is-rc",
          "label_name" -> "alexxx",
          "license" -> "test_license",
          "permalink" -> "permalinky",
          "purchase_url" -> "purchase.com/track",
          "release" -> "1234",
          "release_date" -> "2008/04/29 22:14:25 +0000",
          "sharing" -> "all",
          "tag_list" -> "tags, tags, tags",
          "title" -> "new title",
          "reveal_stats" -> booleanString,
          "reveal_comments" -> booleanString,
          "purchase_title" -> "new title"
        )
      }
    }

    "can handle all varieties of boolean strings" in new BooleanTestContext {
      val boolTypes = Seq("true", "false", "1", "0")

      boolTypes.foreach { boolString =>
        val map = inputMap(boolString)
        val createMetadata = TrackMetadataCreateRequest.fromForm(map)
        createMetadata match {
          case Good(_) => ok
          case _ => ko
        }
      }
    }
  }

  "can write to request body" in new Context {
    val createMetadata = updateTrackJson.as[TrackMetadataCreateRequest]
    val requestBody = Json.stringify(Json.toJson(createMetadata.track))

    requestBody ==== requestBodyExpected
  }
}
