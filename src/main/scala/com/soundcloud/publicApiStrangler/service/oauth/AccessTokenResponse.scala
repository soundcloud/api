package com.soundcloud.publicApiStrangler.service.oauth

case class AccessTokenResponse(
    accessToken: String,
    expiresIn: Option[Int],
    refreshToken: Option[String],
    scope: Seq[String],
    tokenType: String = "bearer"
)

object AccessTokenResponse {
  import play.api.libs.json
  import play.api.libs.json.JsonNaming.SnakeCase
  import play.api.libs.json.{Json, JsonConfiguration, JsString}

  implicit val config = JsonConfiguration(SnakeCase)

  implicit val seqWrites: json.Writes[Seq[String]] = (o: Seq[String]) => JsString(o.mkString(" "))

  implicit def writes: json.Writes[AccessTokenResponse] = Json.writes[AccessTokenResponse]
}
