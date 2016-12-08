package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsObject, JsString, Json}

class CookieHeaderRemovalFilterSpec extends UnitSpecification {

  class CookieCheckingService extends Service[Request, RouterResponse] {
    override def apply(request: Request): Future[RouterResponse] = {
      val headers: Seq[(String, JsString)] =
        request.headerMap.toSeq.map {
          case (k: String, v: String) => (k, JsString(v))
        }
      val response = new ResponseBuilder().json(JsObject(headers)).build
      Future.value(RouterResponse(response, "/foo"))
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
