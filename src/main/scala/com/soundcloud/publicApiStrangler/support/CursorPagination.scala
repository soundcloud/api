package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.nextbff.pagination.{ CursorBasedPage, PageBuilder }
import com.soundcloud.bff.web.BffController
import com.soundcloud.scalakit._
import com.twitter.finagle.http.Request
import com.soundcloud.bff.finagle.ResponseBuilder
import com.twitter.util.Future
import com.twitter.util.Try
import com.twitter.util.Return
import com.twitter.util.Throw
import com.soundcloud.jvmkit.config.Config

class CursorPagination(baseUrl: String) {

  def withPage(request: Request, urn: Urn)(f: CursorBasedPage[Urn] => Future[ResponseBuilder]) =
    Try(PageBuilder(request, baseUrl)(urn).buildCursorBased()) match {
      case Return(page)     => f(page)
      case Throw(exception) => new ResponseBuilder().badRequest.toFuture
    }

}
