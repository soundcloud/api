package com.soundcloud.bff.nextbff.repository

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json.{JsNull, JsValue, Json}

import scala.language.reflectiveCalls

class SafeJsonHandlerSpec extends UnitSpecification {

  trait Context extends Scope {
    val handler =
      new SafeJsonHandler {
        def testJsonObject(response: Response) =
          super.toJsonObject(response)

        def testJsonArray(response: Response) =
          super.toJsonArray(response)
      }
  }

  "extracts json" >> {
    "array" in new Context {
      val jsArray = Json.arr("a", "b")
      val response = jsonResponse(Status.Ok, jsArray)
      handler.testJsonArray(response) ==== jsArray
    }

    "object" in new Context {
      val jsObject = Json.obj("a" -> "b")
      val response = jsonResponse(Status.Ok, jsObject)
      handler.testJsonObject(response) ==== jsObject
    }
  }

  "throws exception for unsuccessful status" >> {
    "array" in new Context {
      val json = Json.arr("bad", "server")
      val response = jsonResponse(Status.InternalServerError, json)
      handler.testJsonArray(response) must throwA[RepositoryException]
    }

    "object" in new Context {
      val json = Json.obj("error" -> "500")
      val response = jsonResponse(Status.InternalServerError, json)
      handler.testJsonObject(response) must throwA[RepositoryException]
    }
  }

  "throws exception for invalid json" >> {
    "array" in new Context {
      val response = jsonResponse(Status.InternalServerError, Json.obj())
      handler.testJsonArray(response) must throwA[RepositoryException]
    }

    "object" in new Context {
      val response = jsonResponse(Status.InternalServerError, JsNull)
      handler.testJsonObject(response) must throwA[RepositoryException]
    }
  }
}

