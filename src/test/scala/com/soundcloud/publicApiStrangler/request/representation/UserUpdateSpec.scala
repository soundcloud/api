package com.soundcloud.publicApiStrangler.request.representation

import com.soundcloud.publicApiStrangler.test.UnitSpecification
import play.api.libs.json.{JsNull, Json}

class UserUpdateSpec extends UnitSpecification {

  "UserUpdate params renders all specified keys" >> {
    val userUpdate = UserUpdate(
      city = Value("city"),
      country_code = Value("us"),
      date_of_birth = Value(UserDateOfBirthUpdate(month = 5, year = 1970)),
      default_license = Value("license"),
      description = Value("description"),
      email = Value("email"),
      first_name = Value("first name"),
      gender = Value("gender"),
      last_name = Value("last name"),
      locale = Value("locale"),
      permalink = Value("permalink"),
      username = Value("username")
    )

    val outputJson = Json.obj(
      "user" -> Json.obj(
        "city" -> "city",
        "country_code" -> "us",
        "date_of_birth" -> Json.obj(
          "month" -> 5,
          "year" -> 1970
        ),
        "description" -> "description",
        "email" -> "email",
        "first_name" -> "first name",
        "gender" -> "gender",
        "last_name" -> "last name",
        "locale" -> "locale",
        "permalink" -> "permalink",
        "username" -> "username"
      ),
      "default_license_simple" -> "license"
    )

    val inputJson = Json.obj(
      "city" -> "city",
      "country_code" -> "us",
      "date_of_birth" -> Json.obj(
        "month" -> 5,
        "year" -> 1970
      ),
      "default_license" -> "license",
      "description" -> "description",
      "email" -> "email",
      "first_name" -> "first name",
      "gender" -> "gender",
      "last_name" -> "last name",
      "locale" -> "locale",
      "permalink" -> "permalink",
      "username" -> "username"
    )

    inputJson.as[UserUpdate] ==== userUpdate
    Json.toJson(userUpdate) ==== outputJson
  }

  "UserUpdate respects null and undefined fields" >> {
    val userUpdate = UserUpdate(
      city = Value("city"),
      country_code = MissingValue,
      date_of_birth = NullValue
    )

    val inputJson = Json.obj(
      "city" -> "city",
      "date_of_birth" -> JsNull
    )

    val outputJson = Json.obj(
      "user" -> Json.obj(
        "city" -> "city",
        "date_of_birth" -> JsNull
      )
    )

    inputJson.as[UserUpdate] ==== userUpdate
    Json.toJson(userUpdate) ==== outputJson
  }

  "UserUpdate params does not render unspecified keys" >> {
    val userUpdate = UserUpdate()

    Json.obj().as[UserUpdate] ==== userUpdate
    Json.toJson(userUpdate) ==== Json.obj("user" -> Json.obj())
  }
}
