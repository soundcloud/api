package com.soundcloud.publicApiStrangler.client.sketchy

import com.soundcloud.jvmkit.module.http.client.Params
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.session.{UserSession, UserSessionBuilder}
import com.soundcloud.publicApiStrangler.client.BigJvmKitConversions._
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse}
import com.twitter.util.{Await, Future}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.{JsNull, JsString}

class SketchyClientSpec extends Specification with Mockito {

  trait AckContext extends Scope {
    val jsonClient = mock[JsonClient]
    val session = new UserSessionBuilder().build()
    val client = new SketchyClient(jsonClient)

    val warningId = 1

    def response: JsonResponse

    jsonClient
      .put(session, Path() / "spam_warnings" / warningId / "ack", Params.empty, Params.empty, None)
      .returns(Future.value(response))
  }

  "#ack" >> {
    "when ack is successful" in new AckContext {
      override def response = JsonResponse(OkStatus, JsNull)

      Await.result(client.ack(session, warningId)) ==== AckOk
    }

    "when warning is not found" in new AckContext {
      override def response = JsonResponse(NotFoundStatus, JsNull)

      Await.result(client.ack(session, warningId)) ==== WarningNotFound
    }

    "when an unknown error occurs" in new AckContext {
      lazy val errorBody = JsString("foobar")

      override def response = JsonResponse(InternalServerErrorStatus, errorBody)

      Await.result(client.ack(session, warningId)) ==== UnknownError(500, errorBody.toString)
    }
  }
}
