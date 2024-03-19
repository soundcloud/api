package com.soundcloud.apipublic.handler

import com.soundcloud.apipublic.Routing
import com.soundcloud.apipublic.test.{HandlerSpecificationScope, UnitSpecification}
import com.soundcloud.jvmkit.module.http.server.Handler
import com.twitter.finagle.http.Method

class MuzookaWebhookHandlerSpec extends UnitSpecification {
  trait Context extends HandlerSpecificationScope {
    val muzookaWebhookHandler = new MuzookaWebhookHandler("F@keAPI_KEY")

    override def routingDefinitions(): List[(Method, String, Handler)] =
      Routing.forMuzookaWebhookHandler(muzookaWebhookHandler)
  }

  "#updateAvatars" >> {
    "on valid request" >> {
      trait SuccessContext extends Context {}

      "returns 200 when request is signed properly" in new SuccessContext {
        val requestHeaders = Map("X-Signature" -> "sha1=49A536256524973A9D406DC7098AF123E5325A5E")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, "Empty Body")
        response.status.code ==== 200
        response.contentString ==== "OK"
      }
    }
    "on request not signed at all" >> {
      "returns 400 when request is signed properly" in new Context {
        val response = post("/muzooka/webhook", Map.empty, Map.empty, "Empty Body")
        response.status.code ==== 400
      }
    }
    "on request not signed with wrong signature" >> {
      "returns 400 when request is signed properly" in new Context {
        val requestHeaders = Map("X-Signature" -> "sha1=374355157FE3BA4B879C487AD020CD38EWRONG")
        val response = post("/muzooka/webhook", Map.empty, requestHeaders, "Empty Body")
        response.status.code ==== 400
      }
    }
  }
}
