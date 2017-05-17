package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.jvmkit.module.http.client.Params
import com.twitter.finagle.http.Request

/**
  * Allows to create pages specifying default values.
  */
case class PageBuilder[T] private(
                                   request: Request,
                                   param: T,
                                   baseUrl: String,
                                   limit: Option[Int] = None,
                                   extraParams: Params = Params.empty) {

  private val defaultLimit = 10

  /**
    * If the limit isn't specified during builder creation and the request
    * doesn't have a limit param, this default limit is used
    */
  def defaultLimit(defaultLimit: Int) =
    limit match {
      case None => this.copy(limit = Some(defaultLimit))
      case Some(_) => this
    }

  /**
    * Allow the specific params to be forwarded to the repository.
    */
  def allowExtraParams(names: Set[String]) =
    this.copy(extraParams = request.params.filterKeys(names.contains))

  /**
    * Builds an offset based page. It uses the standard offset
    * if the request doesn't have the 'offset' param.
    */
  def buildOffsetBased(standardOffset: Int = 0) =
    OffsetBasedPage(
      param,
      baseUrl,
      request.path,
      extraParams,
      intParam(OffsetBasedPage.offsetParam, standardOffset),
      limitWithFallbacks)

  /**
    * Builds a cursor based page. It uses the standard cursor
    * if the request doesn't have the 'cursor' param.
    */
  def buildCursorBased(standardCursor: Option[String] = None) =
    CursorBasedPage(
      param,
      baseUrl,
      request.path,
      extraParams,
      param(CursorBasedPage.cursorParam).orElse(standardCursor),
      limitWithFallbacks)

  private def limitWithFallbacks =
    intParam(Page.limitParam, limit.getOrElse(defaultLimit))

  private def intParam(name: String, default: Int) =
    param(name).map(_.toInt).getOrElse(default)

  private def param(name: String) =
    request.params.get(name)
}

object PageBuilder {

  /**
    * Creates the PageBuilder.
    */
  def apply[T](request: Request, baseUrl: String)(param: T): PageBuilder[T] =
    PageBuilder(request, param, baseUrl)
}
