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

  trait Context extends GenericContext[Map[Urn, StitchCounts]] {
    val jsonClient = mock[JsonClient]

    lazy val client = new StitchClient(jsonClient)

    def resultF = client.countsForTracks(session, trackUrns, userUrn)

    lazy val path = Path() / "bulk"
    val session = anonymousSession

    val trackUrn1 = Urn("soundcloud:tracks:123")
    val trackUrn2 = Urn("soundcloud:tracks:234987")

    val trackUrns = Set(trackUrn1, trackUrn2)
    val userUrn = Urn("soundcloud:users:8700")

    def stitchKey1 = s"${userUrn.getIdentifier}|${trackUrn1.getIdentifier}"
    def stitchKey2 = s"${userUrn.getIdentifier}|${trackUrn2.getIdentifier}"

    def genMockResponseContentBit(cat: String, count1: Int, count2: Int) =
      Json.obj(
        cat -> Json.obj(
          stitchKey1 -> Json.obj(
            "series" -> Seq(
              Json.obj(
                "time" -> 0,
                "count" -> count1))),
          stitchKey2 -> Json.obj(
            "series" -> Seq(
              Json.obj(
                "time" -> 0,
                "count" -> count2)))))

    def mockResponseContents =
      genMockResponseContentBit("p", 111, 222) ++
        genMockResponseContentBit("d", 333, 444) ++
        genMockResponseContentBit("l", 555, 666) ++
        genMockResponseContentBit("c", 777, 888)

    def mockResponseStatus: StatusCode = OkStatus
    def mockResponse =
      Future.value(
        JsonResponse(
          mockResponseStatus,
          mockResponseContents))

    val expectedParams = Params(
      "p" -> s"/ts?c=p.o.t&r=a&k=$stitchKey1&k=$stitchKey2",
      "d" -> s"/ts?c=d.o.t&r=a&k=$stitchKey1&k=$stitchKey2",
      "l" -> s"/ts?c=l.o.t&r=a&k=$stitchKey1&k=$stitchKey2",
      "c" -> s"/ts?c=c.o.t&r=a&k=$stitchKey1&k=$stitchKey2")

    when(jsonClient.get(beTypedEqualTo(session), beTypedEqualTo(Path() / "bulk"), beTypedEqualTo(expectedParams), any))
      .thenReturn(mockResponse)
  }

  "200 response" in new Context {
    result ==== Map(
      trackUrn1 -> StitchCounts(111, 333, 555, 777),
      trackUrn2 -> StitchCounts(222, 444, 666, 888))
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
