package com.soundcloud.publicApiStrangler.client.support

import com.soundcloud.jvmkit.module.http.client.{JsonClient, Params}
import com.soundcloud.jvmkit.module.util.http.Headers
import com.soundcloud.jvmkit.module.util.{Path, Urn}
import com.soundcloud.publicApiStrangler.test.UnitSpecification
import com.twitter.finagle.http.Status
import com.twitter.util.{Await, Future}
import org.mockito.Mockito.when
import play.api.libs.json.{JsArray, JsNull, JsObject, JsString}

class FetchClientSpec extends UnitSpecification {

  trait Context extends Scope {
    val serviceMock = mock[JsonClient]
    val serviceClient = new FetchClient {}
    val path = Path("/test")
  }

  "#fetch" >> {
    "params needs decoding" >> {

      trait ServiceContext extends Context {
        val params = Params("test" -> "%2C")
        val response = jsonResponse(Status.Ok, JsString("test"))

        when(serviceMock.getWithSession(anonymousSession, path, Params("test" -> ","), Headers.empty))
          .thenReturn(Future.value(response))
      }

      "it decodes the params" in new ServiceContext {
        val json = Await.result(serviceClient.fetch(serviceMock, anonymousSession, path, params, Headers.empty)).contentString
        json ==== JsString("test").toString
        there was one(serviceMock).getWithSession(anonymousSession, path, Params("test" -> ","), Headers.empty)
      }
    }
  }

  "#fetchWithoutSession" >> {
    "params needs decoding" >> {

      trait ServiceContext extends Context {
        val params = Params("test" -> "%2C")
        val response = jsonResponse(Status.Ok, JsString("test"))

        when(serviceMock.get(path, Params("test" -> ","), Headers.empty))
          .thenReturn(Future.value(response))
      }

      "it decodes the params" in new ServiceContext {
        val json = Await.result(serviceClient.fetchWithoutSession(serviceMock, path, params, Headers.empty)).contentString
        json ==== JsString("test").toString
        there was one(serviceMock).get(path, Params("test" -> ","), Headers.empty)
      }
    }
  }

  "#fetchByUrns" >> {
    "given urns are empty" >> {
      "returns empty list" in new Context {
        val json = Await.result(serviceClient.fetchByUrns(serviceMock, anonymousSession, path, Set.empty))
        json ==== List.empty
      }
    }

    "given urns are less than limit" >> {
      trait TestContext extends Context {
        val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
        val responseJson = List(JsObject(Seq.empty), JsObject(Seq.empty))
        val response = jsonResponse(Status.Ok, JsArray(responseJson))

        when(serviceMock.getWithSession(anonymousSession, path, urns.toList, Headers.empty)).thenReturn(Future.value(response))
      }

      "makes one call to service" in new TestContext {
        val json = Await.result(serviceClient.fetchByUrns(serviceMock, anonymousSession, path, urns))
        json ==== responseJson
        there was one(serviceMock).getWithSession(anonymousSession, path, urns.toList, Headers.empty)
      }
    }

    "given urns are more than limit" >> {
      "all calls succeed" >> {
        trait TestContext extends Context {
          val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
          val response1Json = List(JsObject(Seq("1" -> JsNull)))
          val response2Json = List(JsObject(Seq("2" -> JsNull)))

          def response(json: List[JsObject]) = jsonResponse(Status.Ok, JsArray(json))

          when(serviceMock.getWithSession(anonymousSession, path, urns.init.toList, Headers.empty)).thenReturn(Future.value(response(response1Json)))
          when(serviceMock.getWithSession(anonymousSession, path, urns.tail.toList, Headers.empty)).thenReturn(Future.value(response(response2Json)))
        }

        "makes multiple calls to service" in new TestContext {
          val json = Await.result(serviceClient.fetchByUrns(serviceMock, anonymousSession, path, urns, 1))
          json ==== response1Json ++ response2Json
          there was one(serviceMock).getWithSession(anonymousSession, path, urns.init.toList, Headers.empty)
          there was one(serviceMock).getWithSession(anonymousSession, path, urns.tail.toList, Headers.empty)
        }
      }

      "one call fails" >> {
        trait TestContext extends Context {
          val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
          val response2Json = List(JsObject(Seq("2" -> JsNull)))

          when(serviceMock.getWithSession(anonymousSession, path, urns.init.toList, Headers.empty))
            .thenReturn(Future.value(jsonResponse(Status.NotFound, JsNull)))
          when(serviceMock.getWithSession(anonymousSession, path, urns.tail.toList, Headers.empty))
            .thenReturn(Future.value(jsonResponse(Status.Ok, JsArray(response2Json))))
        }

        "bails whole transaction" in new TestContext {
          Await.result(serviceClient.fetchByUrns(serviceMock, anonymousSession, path, urns, 1)) must throwA[IllegalStateException]

          there was one(serviceMock).getWithSession(anonymousSession, path, urns.init.toList, Headers.empty)
          there was one(serviceMock).getWithSession(anonymousSession, path, urns.tail.toList, Headers.empty)
        }
      }
    }
  }

  "#fetchByUrnsWithoutSession" >> {
    trait TestContext extends Context {
      val urns = Set(Urn("soundcloud", "users", "1"), Urn("soundcloud", "users", "2"))
      val responseJson = List(JsObject(Seq.empty), JsObject(Seq.empty))
      val response = jsonResponse(Status.Ok, JsArray(responseJson))

      when(serviceMock.get(path, urns.toList, Headers.empty)).thenReturn(Future.value(response))
    }

    "makes one call to service" in new TestContext {
      val json = Await.result(serviceClient.fetchByUrnsWithoutSession(serviceMock, path, urns))
      json ==== responseJson
      there was one(serviceMock).get(path, urns.toList, Headers.empty)
    }
  }
}
