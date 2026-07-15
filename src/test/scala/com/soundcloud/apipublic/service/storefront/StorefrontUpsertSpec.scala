package com.soundcloud.apipublic.service.storefront

import com.soundcloud.apipublic.test.UnitSpecification
import play.api.libs.json.Json

class StorefrontUpsertSpec extends UnitSpecification {

  private val validJson = Json.obj(
    "title" -> "Download now",
    "type" -> "digital",
    "link" -> "https://example.com/my-track",
    "link_title" -> "Download",
    "description" -> "Get the track original files for free.",
    "price" -> "0"
  )

  "StorefrontUpsert.fromJson" should {

    "parse a valid payload" in {
      StorefrontUpsert.fromJson(validJson) must beRight(
        StorefrontUpsert(
          title = "Download now",
          storefrontType = StorefrontType.Digital,
          link = "https://example.com/my-track",
          linkTitle = Some("Download"),
          description = Some("Get the track original files for free."),
          price = Some("0")
        )
      )
    }

    "parse a payload without optional fields" in {
      val json = Json.obj("title" -> "Buy vinyl", "type" -> "vinyl", "link" -> "http://example.com/vinyl")

      StorefrontUpsert.fromJson(json) must beRight(
        StorefrontUpsert(
          title = "Buy vinyl",
          storefrontType = StorefrontType.Vinyl,
          link = "http://example.com/vinyl",
          linkTitle = None,
          description = None,
          price = None
        )
      )
    }

    "treat empty optional fields as absent" in {
      val json = validJson ++ Json.obj("link_title" -> "", "description" -> " ", "price" -> "")

      StorefrontUpsert.fromJson(json) must beRight(
        StorefrontUpsert(
          title = "Download now",
          storefrontType = StorefrontType.Digital,
          link = "https://example.com/my-track",
          linkTitle = None,
          description = None,
          price = None
        )
      )
    }

    "reject a payload without a title" in {
      StorefrontUpsert.fromJson(validJson - "title") must beLeft("title is required and must be a string")
    }

    "reject an empty title" in {
      StorefrontUpsert.fromJson(validJson ++ Json.obj("title" -> "  ")) must beLeft("title must not be empty")
    }

    "reject an unknown type" in {
      StorefrontUpsert.fromJson(validJson ++ Json.obj("type" -> "hologram")) must beLeft(
        s"type must be one of: ${StorefrontType.all.map(_.name).mkString(", ")}"
      )
    }

    "reject a payload without a link" in {
      StorefrontUpsert.fromJson(validJson - "link") must beLeft("link is required and must be a string")
    }

    "reject a link that is not an http(s) URL" in {
      StorefrontUpsert.fromJson(validJson ++ Json.obj("link" -> "ftp://example.com/file")) must beLeft(
        "link must be a valid http(s) URL"
      )
      StorefrontUpsert.fromJson(validJson ++ Json.obj("link" -> "not a url")) must beLeft(
        "link must be a valid http(s) URL"
      )
    }

    "accept a link containing unescaped special characters" in {
      val link = "https://example.com/releases?title=phunk|remix"

      StorefrontUpsert.fromJson(validJson ++ Json.obj("link" -> link)) must beRight(
        StorefrontUpsert(
          title = "Download now",
          storefrontType = StorefrontType.Digital,
          link = link,
          linkTitle = Some("Download"),
          description = Some("Get the track original files for free."),
          price = Some("0")
        )
      )
    }

    "reject a title exceeding the maximum length" in {
      StorefrontUpsert.fromJson(validJson ++ Json.obj("title" -> "a" * 101)) must beLeft(
        "title must not exceed 100 characters"
      )
    }

    "validate length after trimming surrounding whitespace" in {
      val paddedTitle = ("a" * 100) + "   "

      StorefrontUpsert.fromJson(validJson ++ Json.obj("title" -> paddedTitle)) must beRight(
        StorefrontUpsert(
          title = "a" * 100,
          storefrontType = StorefrontType.Digital,
          link = "https://example.com/my-track",
          linkTitle = Some("Download"),
          description = Some("Get the track original files for free."),
          price = Some("0")
        )
      )
    }

    "reject a non-string price" in {
      StorefrontUpsert.fromJson(validJson ++ Json.obj("price" -> 5)) must beLeft("price must be a string")
    }
  }
}
