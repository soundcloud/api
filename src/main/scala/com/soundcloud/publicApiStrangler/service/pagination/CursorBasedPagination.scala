package com.soundcloud.publicApiStrangler.service.pagination

import com.twitter.finagle.http.{ParamMap, Request}

import scala.util.Try

case class CursorBasedPagination(
    baseUrl: String,
    path: String,
    extraParams: ParamMap,
    cursor: Option[String] = None,
    pageSize: Int
) extends Pagination(baseUrl, path) {
  def nextPage(cursor: String): CursorBasedPagination = copy(cursor = Some(cursor))

  def getParams =
    extraParams +
      ((CursorBasedPagination.NormalizedCursorParam, cursor.getOrElse(""))) +
      ((CursorBasedPagination.NormalizedPageSizeParam, pageSize.toString))
}

object CursorBasedPagination extends PaginationHelpers {
  protected[pagination] val DefaultPageSize = 50
  protected[pagination] val NormalizedPageSizeParam = "page_size"
  protected[pagination] val AlternativePageSizeParam = "limit"
  protected[pagination] val NormalizedCursorParam = "cursor"

  private val FalsyValues = Set("0", "")

  def build(request: Request, extraParams: Seq[String] = Seq.empty) = {
    val cursor = request.params
      .get(CursorBasedPagination.NormalizedCursorParam) filterNot FalsyValues.contains

    CursorBasedPagination(
      baseUrl = baseUrl(request),
      path = path(request),
      extraParams = getExtraParams(request, extraParams),
      cursor = cursor,
      pageSize = getPageSize(request.params)
    )
  }

  private def getPageSize(params: ParamMap): Int = {
    params
      .get(CursorBasedPagination.NormalizedPageSizeParam)
      .orElse(params.get(CursorBasedPagination.AlternativePageSizeParam))
      .map(size => Try(size.toInt).getOrElse(DefaultPageSize))
      .getOrElse(DefaultPageSize)
  }
}
