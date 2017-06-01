package com.soundcloud.publicApiStrangler.client.sketchy

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.Path
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.session.UserSessionBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsNull, JsString}

class SketchyClientSpec extends UnitSpecification {

  trait AckContext extends Scope {
    val jsonClient = mock[JsonClient]
    val session = new UserSessionBuilder().build()
    val client = new SketchyClient(jsonClient)

    val warningId = 1

    def response: Response

    jsonClient
      .putWithSession(session, Path() / "spam_warnings" / warningId / "ack", Params.empty, Headers.empty, None)
      .returns(Future.value(response))
  }

  "#ack" >> {
    "when ack is successful" in new AckContext {
      override def response = jsonResponse(Status.Ok, JsNull)

      Await.result(client.ack(session, warningId)) ==== AckOk
    }

    "when warning is not found" in new AckContext {
      override def response = jsonResponse(Status.NotFound, JsNull)

      Await.result(client.ack(session, warningId)) ==== WarningNotFound
    }

    "when an unknown error occurs" in new AckContext {
      lazy val errorBody = JsString("foobar")

      override def response = jsonResponse(Status.InternalServerError, errorBody)

      Await.result(client.ack(session, warningId)) ==== UnknownError(500, errorBody.toString)
    }
  }
}
