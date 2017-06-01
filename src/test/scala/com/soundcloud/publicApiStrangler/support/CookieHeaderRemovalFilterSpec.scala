package com.soundcloud.publicApiStrangler.support

import com.soundcloud.jvmkit.module.http.server.JsonResponseBuilder
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, JsString, Json}

class CookieHeaderRemovalFilterSpec extends UnitSpecification {

  class CookieCheckingService extends Service[Request, Response] {
    override def apply(request: Request): Future[Response] = {
      val headers: Seq[(String, JsString)] =
        request.headerMap.toSeq.map {
          case (k: String, v: String) => (k, JsString(v))
        }
      val response = JsonResponseBuilder().body(Json.stringify(JsObject(headers))).build
      Future.value(response)
    }
  }

  trait Context extends Scope {
    val next = new CookieCheckingService
    val request = Request("/tracks/123/stream.json")
    request.headerMap.set("Cookie", "sc_anonymous_id=111111-222222-333333-444444;")
    lazy val filter = new CookieHeaderRemovalFilter
  }

  private def cookieFor(response: Response): Option[String] = {
    val json = Json.parse(response.contentString)
    (json \ "Cookie").asOpt[String]
  }

  "it removes the Cookie header" in new Context {
    val cookie = cookieFor(Await.result(filter(request, next)))
    cookie must beNone
  }
}
