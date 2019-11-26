package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.jvmkit.module.http.client.{Params, StringParam}
import com.twitter.finagle.http.Request

/**
  * This page implementation allows to use a custom cursor
  * pagination mechanism.
  */
case class CursorBasedPage[T](
    param: T,
    baseUrl: String,
    path: String,
    extraParams: Params,
    cursor: Option[String],
    limit: Int
) extends Page[T] {
  /**
    * Creates the next page for the specified cursor position.
    */
  def next(cursor: String): CursorBasedPage[T] = next(Some(cursor))

  /**
    * Creates the next page for the specified cursor position.
    */
  def next(cursor: Option[String]) = CursorBasedPage(param, baseUrl, path, extraParams, cursor, limit)

  override def params = super.params ++ cursor.map("cursor" -> StringParam(_))
}

object CursorBasedPage {
  val cursorParam = "cursor"

  /**
    * Convenience method to create the page without
    * having to create a PageBuilder.
    */
  def apply[T](request: Request, baseUrl: String)(param: T): CursorBasedPage[T] =
    PageBuilder(request, baseUrl)(param).buildCursorBased()
}
