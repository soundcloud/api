package com.soundcloud.publicApiStrangler.service.pagination

import com.twitter.finagle.http.{ParamMap, Request}
import org.specs2.mutable.Specification

class PaginationSpec extends Specification {
  val host = "api.example.com"
  val baseUrl = s"https://$host"
  val path = "/path/to/endpoint"
  val extraParams = ParamMap(Map("key" -> "value"))
  val pageSize = 2

  def mockRequest(params: Map[String, String]) = {
    val mockRequest = Request(path, params.toSeq: _*)
    mockRequest.headerMap.set("Host", host)
    mockRequest
  }

  def testSuite[P <: Pagination](
      firstPageTest: (P, String),
      subsequentPageTest: (P, String),
      buildFromRequestTest: ((Request, Seq[String]) => P, (P, P, P, P))
  ) = {
    "first page" >> {
      val (page, expectedHref) = firstPageTest

      "normalized href" in {
        page.normalizedHref ==== expectedHref
      }
    }

    "subsequent pages" >> {
      val (page, expectedHref) = subsequentPageTest

      "normalized href" in {
        page.normalizedHref ==== expectedHref
      }
    }

    "building from request" >> {
      val (
        buildFromRequest,
        (
          expectedPageWithoutProxiedParams,
          expectedPageWithProxiedParams,
          expectedPageWithDefaultCursor,
          expectedPageWithDefaultLimit
        )
      ) = buildFromRequestTest
      val proxyParam = Map("keyFromRequest" -> "valueFromRequest")
      val pageSizeParam = Map("page_size" -> pageSize.toString)
      val limitParam = Map("limit" -> pageSize.toString)
      val offsetParam = Map("cursor" -> "1")

      "when not proxying params" in {
        buildFromRequest(mockRequest(proxyParam ++ pageSizeParam ++ offsetParam), Seq.empty) ==== expectedPageWithoutProxiedParams
      }

      "when proxying params" in {
        buildFromRequest(mockRequest(proxyParam ++ pageSizeParam ++ offsetParam), proxyParam.keys.toSeq) ==== expectedPageWithProxiedParams
      }

      "with no cursor param" in {
        buildFromRequest(mockRequest(proxyParam ++ pageSizeParam), Seq.empty) ==== expectedPageWithDefaultCursor
      }

      "with no page size param" in {
        buildFromRequest(mockRequest(offsetParam), Seq.empty) ==== expectedPageWithDefaultLimit
      }

      "with limit param instead page size" in {
        buildFromRequest(mockRequest(limitParam), Seq.empty) ==== expectedPageWithDefaultCursor
      }
    }
  }

  "CursorBasedPagination" >> {
    testSuite(
      (
        CursorBasedPagination(baseUrl, path, extraParams, None, pageSize),
        s"$baseUrl$path?key=value&cursor=&page_size=$pageSize"
      ),
      (
        CursorBasedPagination(baseUrl, path, extraParams, Some("1"), pageSize),
        s"$baseUrl$path?key=value&cursor=1&page_size=$pageSize"
      ),
      (
        Pagination.buildCursorBasedPagination _,
        (
          CursorBasedPagination(baseUrl, path, ParamMap(), Some("1"), pageSize),
          CursorBasedPagination(
            baseUrl,
            path,
            ParamMap(Map("keyFromRequest" -> "valueFromRequest")),
            Some("1"),
            pageSize
          ),
          CursorBasedPagination(baseUrl, path, ParamMap(), None, pageSize),
          CursorBasedPagination(baseUrl, path, ParamMap(), Some("1"), 50)
        )
      )
    )
  }
}
