package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.jvmkit.Urn
import com.soundcloud.scalakit.Path
import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params}
import com.soundcloud.scalakit.test.UnitSpecification
import com.twitter.util.{Await, Future}
import org.mockito.Mockito._
import play.api.libs.json.{JsNull, JsObject, Json}

class StitchClientSpec extends UnitSpecification {
  trait GenericContext[T] extends Scope {
    def resultF: Future[T]
    def result = Await.result(resultF)
    def resultT = Await.result(resultF.liftToTry)
  }

  trait Context extends GenericContext[Map[Urn, StitchCounts]] {
    val jsonClient = mock[JsonClient]

    lazy val client = new StitchClient(jsonClient)

    def resultF = client.countsForTracksByUser(session, userUrn, trackUrns, 2)

    lazy val path = Path() / "bulk"
    val session = anonymousSession

    val trackUrn1 = Urn("soundcloud:tracks:123")
    val trackUrn2 = Urn("soundcloud:tracks:234987")
    val trackUrn3 = Urn("soundcloud:tracks:129389")

    val trackUrns = Set(trackUrn1, trackUrn2, trackUrn3)
    val userUrn = Urn("soundcloud:users:8700")

    def stitchKey1 = s"${userUrn.getIdentifier}|${trackUrn1.getIdentifier}"
    def stitchKey2 = s"${userUrn.getIdentifier}|${trackUrn2.getIdentifier}"
    def stitchKey3 = s"${userUrn.getIdentifier}|${trackUrn3.getIdentifier}"

    def genMockResponseContentBit(cat: String, keys: List[(String, Int)]) = {
      val seriesPerKey = keys.map { case (key, count) =>
        key -> Json.obj(
          "series" -> Seq(Json.obj(
            "time" -> 0,
            "count" -> count)))
      }

      Json.obj(cat -> JsObject(seriesPerKey))
    }

    def mockResponseContentsFirstBatch =
      genMockResponseContentBit("plays", List((stitchKey1, 111), (stitchKey2, 222))) ++
        genMockResponseContentBit("downloads", List((stitchKey1, 333), (stitchKey2, 444))) ++
        genMockResponseContentBit("likes", List((stitchKey1, 555), (stitchKey2, 666))) ++
        genMockResponseContentBit("comments", List((stitchKey1, 777), (stitchKey2, 888))) ++
        genMockResponseContentBit("reposts", List((stitchKey1, 999), (stitchKey2, 123)))

    def mockResponseContentsSecondBatch =
      genMockResponseContentBit("plays", List((stitchKey3, 567))) ++
        genMockResponseContentBit("downloads", List((stitchKey3, 678))) ++
        genMockResponseContentBit("likes", List((stitchKey3, 789))) ++
        genMockResponseContentBit("comments", List((stitchKey3, 890))) ++
        genMockResponseContentBit("reposts", List((stitchKey3, 901)))

    def mockResponseStatus: StatusCode = OkStatus
    def mockResponseFirstBatch =
      Future.value(
        JsonResponse(
          mockResponseStatus,
          mockResponseContentsFirstBatch))

    def mockResponseSecondBatch =
      Future.value(
        JsonResponse(
          mockResponseStatus,
          mockResponseContentsSecondBatch))

    val expectedParamsFirstBatch = Params(
      "plays" -> s"/ts?category=p.o.t&resolution=alltime&k=$stitchKey1&k=$stitchKey2",
      "downloads" -> s"/ts?category=d.o.t&resolution=alltime&k=$stitchKey1&k=$stitchKey2",
      "likes" -> s"/ts?category=l.o.t&minus-category=n.l.o.t&resolution=alltime&k=$stitchKey1&k=$stitchKey2",
      "comments" -> s"/ts?category=c.o.t&minus-category=n.c.o.t&resolution=alltime&k=$stitchKey1&k=$stitchKey2",
      "reposts" -> s"/ts?category=r.o.t&minus-category=n.r.o.t&resolution=alltime&k=$stitchKey1&k=$stitchKey2")

    val expectedParamsSecondBatch = Params(
      "plays" -> s"/ts?category=p.o.t&resolution=alltime&k=$stitchKey3",
      "downloads" -> s"/ts?category=d.o.t&resolution=alltime&k=$stitchKey3",
      "likes" -> s"/ts?category=l.o.t&minus-category=n.l.o.t&resolution=alltime&k=$stitchKey3",
      "comments" -> s"/ts?category=c.o.t&minus-category=n.c.o.t&resolution=alltime&k=$stitchKey3",
      "reposts" -> s"/ts?category=r.o.t&minus-category=n.r.o.t&resolution=alltime&k=$stitchKey3")

    when(jsonClient.get(session, Path() / "bulk", expectedParamsFirstBatch, Params.empty)).thenReturn(mockResponseFirstBatch)
    when(jsonClient.get(session, Path() / "bulk", expectedParamsSecondBatch, Params.empty)).thenReturn(mockResponseSecondBatch)
  }

  "200 response" in new Context {
    result ==== Map(
      trackUrn1 -> StitchCounts(111, 333, 555, 777, 999),
      trackUrn2 -> StitchCounts(222, 444, 666, 888, 123),
      trackUrn3 -> StitchCounts(567, 678, 789, 890, 901))
  }

  "500 response" in new Context {
    override def mockResponseStatus = InternalServerErrorStatus
    resultT.isThrow === true
  }

  "exception response" in new Context {
    override def mockResponseFirstBatch = Future.exception(new RuntimeException("kaboom"))
    resultT.isThrow === true
  }
}
