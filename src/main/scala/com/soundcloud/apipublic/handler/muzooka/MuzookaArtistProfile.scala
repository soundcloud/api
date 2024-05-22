package com.soundcloud.apipublic.handler.muzooka

import play.api.libs.json.{Json, Reads}

case class Image(
    width: Int,
    height: Int,
    ratio: Option[String],
    name: String,
    url: String
)

case class SocialLink(
    `type`: String,
    id: Option[String],
    url: String
)

case class Links(
    self: String,
    videos: String,
    images: String,
    muzookaUrl: String
)

case class MuzookaArtist(
    name: String,
    city: Option[String],
    province: Option[String],
    country: Option[String],
    website: Option[String],
    bio: Option[String],
    id: String,
    image: Option[List[Image]],
    imageSource: Option[String],
    socialLinks: Option[List[SocialLink]],
    links: Option[Links]
)

object JsonFormats {
  implicit val imageReads: Reads[Image] = Json.reads[Image]
  implicit val socialLinkReads: Reads[SocialLink] = Json.reads[SocialLink]
  implicit val linksReads: Reads[Links] = Json.reads[Links]
  implicit val artistReads: Reads[MuzookaArtist] = Json.reads[MuzookaArtist]
}
