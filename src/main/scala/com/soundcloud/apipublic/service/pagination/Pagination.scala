package com.soundcloud.apipublic.service.pagination

import com.twitter.finagle.http.{ParamMap, Request}
import play.api.libs.json.{JsString, Writes}

abstract class Pagination(
    baseUrl: String,
    path: String
) {
  implicit def paginationWrites = Writes[Pagination] { p: Pagination =>
    JsString(p.normalizedHref)
  }

  def getParams: ParamMap

  def normalizedHref: String =
    Request(
      s"$baseUrl$path",
      getParams.toSeq.sorted: _*
    ).uri

}

trait PaginationHelpers {

  def path(request: Request): String = request.path

  def getExtraParams(request: Request, paramNames: Seq[String]): ParamMap = {
    ParamMap(request.params.filter {
      case (name, _) =>
        paramNames.contains(name)
    })
  }
}
