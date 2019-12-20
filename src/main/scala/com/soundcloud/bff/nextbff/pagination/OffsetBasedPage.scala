package com.soundcloud.bff.nextbff.pagination

import com.soundcloud.jvmkit.module.http.client.Params
import com.twitter.finagle.http.Request

/**
  * This page implementation uses the offset/limit
  * pagination approach.
  */
case class OffsetBasedPage[T](param: T, baseUrl: String, path: String, extraParams: Params, offset: Int, limit: Int)
    extends Page[T] {

  /**
    * Infers the next page using offset + limit.
    */
  def next: OffsetBasedPage[T] = next(offset + limit)

  /**
    * Creates the next page using the specified offset
    */
  def next(offset: Int) = OffsetBasedPage(param, baseUrl, path, extraParams, offset, limit)

  override def params = super.params + ("offset" -> offset.toString)
}

object OffsetBasedPage {
  val limitParam = "limit"
  val offsetParam = "offset"
  val standardOffset = 0

  /**
    * Convenience menthod to create the page without
    * having to create a PageBuilder.
    */
  def apply[T](request: Request, baseUrl: String)(param: T): OffsetBasedPage[T] =
    PageBuilder(request, baseUrl)(param).buildOffsetBased()
}
