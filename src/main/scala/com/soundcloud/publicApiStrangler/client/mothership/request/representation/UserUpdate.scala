package com.soundcloud.publicApiStrangler.client.mothership.request.representation

import play.api.libs.json._

// Taken from list here:
// https://github.com/soundcloud/soundcloud/blob/master/vendor/plugins/moshimoshi/app/controllers/moshi_moshi/users_controller.rb#L6-L8
case class UserUpdate(
    city: NullableValue[String] = MissingValue,
    country_code: NullableValue[String] = MissingValue,
    date_of_birth: NullableValue[UserDateOfBirthUpdate] = MissingValue,
    default_license: NullableValue[String] = MissingValue,
    description: NullableValue[String] = MissingValue,
    email: NullableValue[String] = MissingValue,
    first_name: NullableValue[String] = MissingValue,
    gender: NullableValue[String] = MissingValue,
    last_name: NullableValue[String] = MissingValue,
    locale: NullableValue[String] = MissingValue,
    permalink: NullableValue[String] = MissingValue,
    username: NullableValue[String] = MissingValue
)

object UserUpdate {
  implicit val format = new Format[UserUpdate] {
    override def writes(o: UserUpdate) = {
      // Write out a nested object for upstream consumers (right now, moshimoshi/okidoki)
      // This means nesting all the user params under a "user" property, and having one additional
      // (differently named) property for the default license
      JsObject(
        Seq(
          "user" -> JsObject(
            o.city.toOptionalJsValue.map("city" -> _).toSeq ++
              o.country_code.toOptionalJsValue.map("country_code" -> _).toSeq ++
              o.date_of_birth.toOptionalJsValue.map("date_of_birth" -> _).toSeq ++
              o.description.toOptionalJsValue.map("description" -> _).toSeq ++
              o.email.toOptionalJsValue.map("email" -> _).toSeq ++
              o.first_name.toOptionalJsValue.map("first_name" -> _).toSeq ++
              o.gender.toOptionalJsValue.map("gender" -> _).toSeq ++
              o.last_name.toOptionalJsValue.map("last_name" -> _).toSeq ++
              o.locale.toOptionalJsValue.map("locale" -> _).toSeq ++
              o.permalink.toOptionalJsValue.map("permalink" -> _).toSeq ++
              o.username.toOptionalJsValue.map("username" -> _).toSeq
          )
        ) ++
          o.default_license.toOptionalJsValue.map("default_license_simple" -> _).toSeq
      )
    }

    override def reads(json: JsValue) = JsSuccess {
      UserUpdate(
        city = NullableValue.read[String](json \ "city"),
        country_code = NullableValue.read[String](json \ "country_code"),
        date_of_birth = NullableValue.read[UserDateOfBirthUpdate](json \ "date_of_birth"),
        default_license = NullableValue.read[String](json \ "default_license"),
        description = NullableValue.read[String](json \ "description"),
        email = NullableValue.read[String](json \ "email"),
        first_name = NullableValue.read[String](json \ "first_name"),
        gender = NullableValue.read[String](json \ "gender"),
        last_name = NullableValue.read[String](json \ "last_name"),
        locale = NullableValue.read[String](json \ "locale"),
        permalink = NullableValue.read[String](json \ "permalink"),
        username = NullableValue.read[String](json \ "username")
      )
    }
  }
}
