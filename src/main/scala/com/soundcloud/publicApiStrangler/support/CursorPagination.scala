package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.nextbff.pagination.{CursorBasedPage, PageBuilder}
import com.soundcloud.jvmkit.module.http.server.ResponseBuilder
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.{Future, Return, Throw, Try}

class CursorPagination(baseUrl: String) {

  def withPage[T](request: Request, param: T)(f: CursorBasedPage[T] => Future[Response]) =
    Try(PageBuilder(request, baseUrl)(param).buildCursorBased()) match {
      case Return(page) => f(page)
      case Throw(exception) => Future.value(ResponseBuilder.badRequest())
    }

}
