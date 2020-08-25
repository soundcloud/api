package com.soundcloud.publicApiStrangler.service.pagination

import com.twitter.finagle.http.{ParamMap, Request}

case class OffsetBasedPagination(
    baseUrl: String,
    path: String,
    extraParams: ParamMap,
    offset: Option[Int] = None,
    limit: Int
) extends Pagination(baseUrl, path) {
  def nextPage(offset: Int): OffsetBasedPagination = copy(offset = Some(offset))

  override def getParams =
    extraParams +
      ((OffsetBasedPagination.NormalizedOffsetParam, offset.getOrElse(0))) +
      ((OffsetBasedPagination.NormalizedLimitParam, limit))
}

object OffsetBasedPagination extends PaginationHelpers {
  protected[pagination] val DefaultLimit = 50
  protected[pagination] val NormalizedLimitParam = "limit"
  protected[pagination] val NormalizedOffsetParam = "offset"

  def build(request: Request, extraParams: Seq[String] = Seq.empty) = {
    val offset = request.params
      .get(OffsetBasedPagination.NormalizedOffsetParam)
      .map(_.toInt)

    OffsetBasedPagination(
      baseUrl = baseUrl(request),
      path = path(request),
      extraParams = getExtraParams(request, extraParams),
      limit = getPageSize(request.params),
      offset = offset
    )
  }

  private def getPageSize(params: ParamMap): Int = {
    params
      .get(OffsetBasedPagination.NormalizedLimitParam)
      .map(_.toInt)
      .getOrElse(DefaultLimit)
  }
}
