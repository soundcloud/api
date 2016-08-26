package com.soundcloud.publicApiStrangler.client.stitch

import com.soundcloud.scalakit.finagle.http.{InternalServerErrorStatus, NotFoundStatus, OkStatus, StatusCode}
import com.soundcloud.scalakit.finagle.jsonservice.{JsonClient, JsonResponse, Params, StringParam}
import com.soundcloud.scalakit.test.UnitSpecification
import com.soundcloud.scalakit.{Path, Urn}
import com.twitter.util.{Await, Future}
import play.api.libs.json.{JsArray, Json}
import org.mockito.Mockito._

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

    lazy val path = Path() / "ts"
    val session = anonymousSession
    val urn = Urn("soundcloud:tracks:123")
    val userUrn = Urn("soundcloud:users:8700")

    def stitchKey = s"${userUrn.getIdentifier}|${urn.getIdentifier}"
    def genMockResponseContents(count: Int) =
      Json.obj(
        stitchKey -> Json.obj(
          "series" -> Seq(
            Json.obj(
              "time" -> 0,
              "count" -> count))))

    def mockPlaybackCount: Int = 111
    def mockPlaybackResponseStatus: StatusCode = OkStatus
    def mockPlaybackResponseContents = genMockResponseContents(mockPlaybackCount)
    def mockPlaybackResponse =
      Future.value(
        JsonResponse(
          mockPlaybackResponseStatus,
          mockPlaybackResponseContents))

    def mockDownloadCount: Int = 222
    def mockDownloadResponseStatus: StatusCode = OkStatus
    def mockDownloadResponseContents = genMockResponseContents(mockDownloadCount)
    def mockDownloadResponse =
      Future.value(
        JsonResponse(
          mockDownloadResponseStatus,
          mockDownloadResponseContents))

    def mockFavoritingsCount: Int = 333
    def mockFavoritingsResponseStatus: StatusCode = OkStatus
    def mockFavoritingsResponseContents = genMockResponseContents(mockFavoritingsCount)
    def mockFavoritingsResponse =
      Future.value(
        JsonResponse(
          mockFavoritingsResponseStatus,
          mockFavoritingsResponseContents))

    def mockCommentsCount: Int = 444
    def mockCommentsResponseStatus: StatusCode = OkStatus
    def mockCommentsResponseContents = genMockResponseContents(mockCommentsCount)
    def mockCommentsResponse =
      Future.value(
        JsonResponse(
          mockCommentsResponseStatus,
          mockCommentsResponseContents))

    def e[T](x: T) = beTypedEqualTo(x)
    val expectedParams = Map("keys" -> StringParam(stitchKey), "r" -> StringParam("a"))
    def paramsForCat(cat: String) = expectedParams + ("c" -> StringParam(cat))

    when(jsonClient.get(e(session), e(path), e(paramsForCat("p.o.t")), any))
      .thenReturn(mockPlaybackResponse)
    when(jsonClient.get(e(session), e(path), e(paramsForCat("d.o.t")), any))
      .thenReturn(mockDownloadResponse)
    when(jsonClient.get(e(session), e(path), e(paramsForCat("l.o.t")), any))
      .thenReturn(mockFavoritingsResponse)
    when(jsonClient.get(e(session), e(path), e(paramsForCat("c.o.t")), any))
      .thenReturn(mockCommentsResponse)
  }

  "200 response" in new Context {
    result ==== StitchCounts(111, 222, 333, 444)
  }

  "500 response" in new Context {
    override def mockPlaybackResponseStatus = InternalServerErrorStatus
    resultT.isThrow === true
  }

  "exception response" in new Context {
    override def mockPlaybackResponse = Future.exception(new RuntimeException("kaboom"))
    resultT.isThrow === true
  }
}
