package com.soundcloud.publicApiStrangler.authorization

import com.soundcloud.jvmkit.Urn
import com.soundcloud.jvmkit.policies.{ContentAuthorization, ContentPolicy, MonetizationModel, Reason}
import com.soundcloud.scalakit.test.UnitSpecification
import play.api.libs.json.{JsObject, Json}

class TrackSpec extends UnitSpecification {
  MonetizationModel.values().foreach(monetizationModel => {
    ContentPolicy.values().foreach(contentPolicy => {
      s"it serializes with $monetizationModel and $contentPolicy" in new Scope {
        val contentAuthorization = new ContentAuthorization(
          Urn("soundcloud:irrelevant:1"),
          contentPolicy,
          Reason.CLIENT_APPLICATION,
          monetizationModel)

        val result = new Track(Json.parse("""{"something":"else"}"""))
          .withContentAuthorization(contentAuthorization)

        result ==== Json.parse(
          s"""
            |{
            |  "something": "else",
            |  "policy": "${contentPolicy.toString}",
            |  "monetization_model": "${monetizationModel.toString}"
            |}""".stripMargin).as[JsObject]
      }
    })
  })
}
