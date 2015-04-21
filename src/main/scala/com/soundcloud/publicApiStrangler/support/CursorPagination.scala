package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.finagle.ResponseBuilder
import com.soundcloud.bff.nextbff.pagination.{CursorBasedPage, PageBuilder}
import com.twitter.finagle.http.Request
import com.twitter.util.{Future, Return, Throw, Try}

class CursorPagination(baseUrl: String) {

  def withPage[T](request: Request, param: T)(f: CursorBasedPage[T] => Future[ResponseBuilder]) =
    Try(PageBuilder(request, baseUrl)(param).buildCursorBased()) match {
      case Return(page)     => f(page)
      case Throw(exception) => new ResponseBuilder().badRequest.toFuture
    }

}
