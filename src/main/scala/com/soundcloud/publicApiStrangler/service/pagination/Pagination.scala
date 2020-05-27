package com.soundcloud.publicApiStrangler.service.pagination

import com.twitter.finagle.http.{ParamMap, Request}
import play.api.libs.json.{JsString, Writes}

abstract class Pagination(
    baseUrl: String,
    path: String,
    extraParams: ParamMap,
    pageCursor: Option[String] = None,
    pageSize: Int,
    firstPageCursor: String
) {

  def normalizedHref =
    Request(
      s"$baseUrl$path",
      getParams.toSeq: _*
    ).uri

  def getParams =
    extraParams +
      ((Pagination.NormalizedCursorParam, pageCursor.getOrElse(firstPageCursor))) +
      ((Pagination.NormalizedPageSizeParam, pageSize.toString))
}

case class CursorBasedPagination(
    baseUrl: String,
    path: String,
    extraParams: ParamMap,
    cursor: Option[String] = None,
    pageSize: Int
) extends Pagination(baseUrl, path, extraParams, cursor, pageSize, "") {
  def nextPage(cursor: String) = copy(cursor = Some(cursor))
}

object Pagination {
  implicit def paginationWrites = Writes[Pagination] { p: Pagination =>
    JsString(p.normalizedHref)
  }

  protected[pagination] val DefaultPageSize = 50
  protected[pagination] val NormalizedPageSizeParam = "page_size"
  protected[pagination] val NormalizedCursorParam = "cursor"

  private val FalsyValues = Set("0", "")

  def buildCursorBasedPagination(request: Request, extraParams: Seq[String] = Seq.empty) = {
    CursorBasedPagination(
      baseUrl = baseUrl(request),
      path = path(request),
      extraParams = getExtraParams(request, extraParams),
      cursor = request.params.get(Pagination.NormalizedCursorParam).filterNot(FalsyValues.contains),
      pageSize = request.params.get(Pagination.NormalizedPageSizeParam).map(_.toInt).getOrElse(DefaultPageSize)
    )
  }

  private def baseUrl(request: Request) = {
    s"https://${request.host.get}"
  }

  private def path(request: Request) = request.path

  private def getExtraParams(request: Request, paramNames: Seq[String]) = {
    ParamMap(request.params)
  }
}
