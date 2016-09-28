package com.soundcloud.bff.nextbff.repository

import com.soundcloud.bff.test.UnitSpecification
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.JsonResponse
import play.api.libs.json.{JsNull, Json}

import scala.language.reflectiveCalls

class SafeJsonHandlerSpec extends UnitSpecification {

  trait Context extends Scope {
    val handler =
      new SafeJsonHandler {
        def testJsonObject(response: JsonResponse) =
          super.toJsonObject(response)

        def testJsonArray(response: JsonResponse) =
          super.toJsonArray(response)
      }
  }

  "extracts json" >> {
    "array" in new Context {
      val jsArray = Json.arr("a", "b")
      val response = JsonResponse(OkStatus, jsArray)
      handler.testJsonArray(response) ==== jsArray
    }

    "object" in new Context {
      val jsObject = Json.obj("a" -> "b")
      val response = JsonResponse(OkStatus, jsObject)
      handler.testJsonObject(response) ==== jsObject
    }
  }

  "throws exception for unsuccessful status" >> {
    "array" in new Context {
      val json = Json.arr("bad", "server")
      val response = JsonResponse(InternalServerErrorStatus, json)
      handler.testJsonArray(response) must throwA[RepositoryException]
    }

    "object" in new Context {
      val json = Json.obj("error" -> "500")
      val response = JsonResponse(InternalServerErrorStatus, json)
      handler.testJsonObject(response) must throwA[RepositoryException]
    }
  }

  "throws exception for invalid json" >> {
    "array" in new Context {
      val response = JsonResponse(InternalServerErrorStatus, Json.obj())
      handler.testJsonArray(response) must throwA[RepositoryException]
    }

    "object" in new Context {
      val response = JsonResponse(InternalServerErrorStatus, JsNull)
      handler.testJsonObject(response) must throwA[RepositoryException]
    }
  }
}

