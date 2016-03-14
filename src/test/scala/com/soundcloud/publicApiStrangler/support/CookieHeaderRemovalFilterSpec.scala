package com.soundcloud.publicApiStrangler.support

import com.soundcloud.scalakit.finagle.http.RouterResponse
import com.soundcloud.scalakit.test.{UnitSpecification, VerifiedMocks}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Status, Response, Request}
import com.twitter.util.{Await, Future}
import com.soundcloud.bff.finagle.ResponseBuilder
import play.api.libs.json.{JsObject, JsString, Json}

class CookieHeaderRemovalFilterSpec extends UnitSpecification {

  class CookieCheckingService extends Service[Request, RouterResponse] {
    override def apply(request: Request): Future[RouterResponse] = {
      val headers: Seq[(String, JsString)] =
        request.headerMap.toSeq.map {
          case (k: String, v: String) => (k, JsString(v))
        }
      val response = new ResponseBuilder().typedJson(JsObject(headers)).build
      Future.value(RouterResponse(response, "/foo"))
    }
  }

  trait Context extends VerifiedMocks {
    val next = new CookieCheckingService
    val enabled: () => Future[Boolean]

    val request = Request("/tracks/123/stream.json")
    request.headerMap.set("Cookie", "sc_anonymous_id=111111-222222-333333-444444;")

    lazy val filter = new CookieHeaderRemovalFilter(enabled)
  }

  trait EnabledContext extends Context {
    val enabled = () => Future.True
  }

  trait DisabledContext extends Context {
    val enabled = () => Future.False
  }

  private def cookieFor(response: Response): Option[String] = {
    val json = Json.parse(response.contentString)
    (json \ "Cookie").asOpt[String]
  }

  "when enabled" >> {
    "it removes the Cookie header" in new EnabledContext {
      val cookie = cookieFor(Await.result(filter(request, next)))
      cookie must beNone
    }
  }

  "when disabled" >> {
    "it leaves the Cookie header intact" in new DisabledContext {
      val cookie = cookieFor(Await.result(filter(request, next)))
      cookie must beSome("sc_anonymous_id=111111-222222-333333-444444;")
    }
  }
}
