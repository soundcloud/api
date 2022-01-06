package com.soundcloud.apipublic.client.mothership.request.representation

import play.api.libs.json.Json

case class S3Artwork(bucket: Option[String], filename: Option[String])

object S3Artwork {
  implicit val format = Json.format[S3Artwork]
}
