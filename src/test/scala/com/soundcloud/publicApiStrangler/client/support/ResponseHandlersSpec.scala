package com.soundcloud.publicApiStrangler.client.support

import com.soundcloud.publicApiStrangler.client.support.ResponseHandlers._
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.{Response, Status}
import play.api.libs.json._

class ResponseHandlersSpec extends UnitSpecification {
  def JsonResponse(status: Status, json: JsValue): Response = ???

  "JsValueResponse" >> {
    "should return JsValue when 200 status" in new Scope {
      JsValueResponse(jsonResponse(Status.Ok, JsString("foo"))) ==== JsString("foo")
    }

    "should return JsValue when 20x status" in new Scope {
      JsValueResponse(jsonResponse(Status.Created, JsString("foo"))) ==== JsString("foo")
      JsValueResponse(jsonResponse(Status.Accepted, JsString("foo"))) ==== JsString("foo")
    }
  }

  "ListResponse" >> {
    "should return List[JsObject] when 200 status" in new Scope {
      ListResponse(jsonResponse(Status.Ok, JsArray(List(JsObject(List(("foo", JsString("bar")))))))) ==== List(
        JsObject(List(("foo", JsString("bar"))))
      )
    }

    "should throw IllegalStateException when not 200 status" in new Scope {
      ListResponse(jsonResponse(Status.NotFound, JsArray(List(JsObject(List(("foo", JsString("bar")))))))) must
        throwAn[IllegalStateException]("Invalid response: status=404,body=\\[\\{\"foo\":\"bar\"\\}\\]")
    }
  }

  "StringSetResponse" >> {
    "should return Set[String] when 200 status" in new Scope {
      StringSetResponse(jsonResponse(Status.Ok, JsArray(List(JsString("foo"), JsString("bar"))))) ==== Set("foo", "bar")
    }

    "should throw IllegalStateException when not 200 status" in new Scope {
      StringSetResponse(jsonResponse(Status.NotFound, JsArray(List(JsString("foo"), JsString("bar"))))) must
        throwAn[IllegalStateException]("Invalid response: status=404,body=\\[\"foo\",\"bar\"\\]")
    }
  }

  "OptionalSingleItem" >> {
    "should return None when 404 status" in new Scope {
      OptionalSingleItem(jsonResponse(Status.NotFound, JsString("foo"))) ==== None
    }

    "should return None when 2xx status but no body" in new Scope {
      OptionalSingleItem(jsonResponse(Status.Ok, JsNull)) ==== None
    }

    "should return Some(body) when 2xx status with body" in new Scope {
      OptionalSingleItem(jsonResponse(Status.NonAuthoritativeInformation, JsObject(List(("foo", JsString("bar")))))) ====
        Some(JsObject(List(("foo", JsString("bar")))))
    }

    "should throw IllegalStateException when not 404 or 2xx status" in new Scope {
      OptionalSingleItem(jsonResponse(Status.InternalServerError, JsString("foo"))) must
        throwAn[IllegalStateException]("Invalid response: status=500,body=\"foo\"")
    }
  }

  "SingleItem" >> {
    "should throw IllegalStateException when 404 status" in new Scope {
      SingleItem(jsonResponse(Status.NotFound, JsString("foo"))) must
        throwAn[IllegalStateException]("Invalid response: status=404,body=\"foo\"")
    }

    "should throw IllegalStateException when 2xx status but no body" in new Scope {
      SingleItem(jsonResponse(Status.Ok, JsNull)) must
        throwAn[IllegalStateException]("Invalid response: status=200,body=null")
    }

    "should return body when 2xx status with body" in new Scope {
      SingleItem(jsonResponse(Status.NonAuthoritativeInformation, JsObject(List(("foo", JsString("bar")))))) ==== JsObject(
        List(("foo", JsString("bar")))
      )
    }

    "should throw IllegalStateException when not 404 or 2xx status" in new Scope {
      SingleItem(jsonResponse(Status.InternalServerError, JsString("foo"))) must
        throwAn[IllegalStateException]("Invalid response: status=500,body=\"foo\"")
    }
  }

  "Status.BooleanByResponse" >> {
    "should return true when 200 status" in new Scope {
      BooleanByStatusResponse(jsonResponse(Status.Ok, JsNull)) ==== true
    }

    "should return false when 404 status" in new Scope {
      BooleanByStatusResponse(jsonResponse(Status.NotFound, JsNull)) ==== false
    }

    "should throw IllegalStateException when not 2xx or 404 status" in new Scope {
      BooleanByStatusResponse(jsonResponse(Status.TemporaryRedirect, JsNull)) must
        throwAn[IllegalStateException]("Invalid response: status=307,body=null")
    }
  }
}
