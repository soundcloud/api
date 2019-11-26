package com.soundcloud.bff.nextbff.pagination

import com.netaporter.uri.Parameters.ParamSeq
import com.netaporter.uri.config.UriConfig
import com.netaporter.uri.encoding.percentEncode
import com.netaporter.uri.{QueryString, StringPathPart, Uri}
import com.soundcloud.jvmkit.module.http.client.Params

/**
  * Class containing information needed for pagination.
  */
trait Page[T] {
  val param: T
  val baseUrl: String
  val path: String
  val extraParams: Params
  val limit: Int

  /**
    * Pagination params for passing to services
    */
  def params = Params(Page.limitParam -> limit.toString)

  /**
    * Complete uri for embedding in responses
    */
  def href = baseUrl + pageUrlPath

  protected lazy val pageUrlPath = Uri(
    pathParts = path.split('/').filterNot(_.isEmpty).toSeq.map(StringPathPart.apply),
    query = QueryString(allQueryParams)
  ).toString(UriConfig(percentEncode))

  private[this] lazy val allQueryParams: ParamSeq =
    (extraParams ++ params).mapValues(_.value.headOption).toSeq
}

object Page {
  val limitParam = "limit"
}
