package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.Json

class StitchClientSpec extends UnitSpecification {
  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
    def resultT = Await.result(resultF.liftToTry)
  }

  trait Context extends GenericContext[StitchCounts] {
    val jsonClient = mock[JsonClient]

    lazy val client = new StitchClient(jsonClient)

    def resultF = client.countsForTrack(session, urn, userUrn)

    lazy val path = Path() / "bulk"
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")
    val userUrn = Urn("soundcloud:users:8700")

    def stitchKey = s"${userUrn.getIdentifier}|${urn.getIdentifier}"

    def genMockResponseContentBit(cat: String, count: Int) =
      Json.obj(
        cat -> Json.obj(
          stitchKey -> Json.obj(
            "series" -> Seq(
              Json.obj(
                "time" -> 0,
                "count" -> count)))))

    def mockResponseContents =
      genMockResponseContentBit("p", 111) ++
        genMockResponseContentBit("d", 222) ++
        genMockResponseContentBit("l", 333) ++
        genMockResponseContentBit("c", 444)

    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse =
      Future.value(
        JsonResponse(
          mockResponseStatus,
          mockResponseContents))

    val expectedParams = Params(
      "p" -> s"/ts?c=p.o.t&r=a&k=$stitchKey",
      "d" -> s"/ts?c=d.o.t&r=a&k=$stitchKey",
      "l" -> s"/ts?c=l.o.t&r=a&k=$stitchKey",
      "c" -> s"/ts?c=c.o.t&r=a&k=$stitchKey")

    when(jsonClient.get(beTypedEqualTo(session), beTypedEqualTo(Path() / "bulk"), beTypedEqualTo(expectedParams), any))
      .thenReturn(mockResponse)
  }

  "200 response" in new Context {
    result ==== StitchCounts(111, 222, 333, 444)
  }

  "500 response" in new Context {
    override def mockResponseStatus = InternalServerErrorStatus
    resultT.isThrow === true
  }

  "exception response" in new Context {
    override def mockResponse = Future.exception(new RuntimeException("kaboom"))
    resultT.isThrow === true
  }
}
