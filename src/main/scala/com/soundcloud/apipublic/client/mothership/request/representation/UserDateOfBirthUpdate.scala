package com.soundcloud.apipublic.client.mothership.request.representation

import play.api.libs.json._

case class UserDateOfBirthUpdate(month: Int, year: Int)

object UserDateOfBirthUpdate {
  implicit val format = Json.format[UserDateOfBirthUpdate]
}
