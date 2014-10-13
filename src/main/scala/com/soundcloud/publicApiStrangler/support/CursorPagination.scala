package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.nextbff.pagination.{CursorBasedPage, PageBuilder}
import com.soundcloud.bff.web.BffController
import com.soundcloud.scalakit._
import com.twitter.finagle.http.Request

trait CursorPagination extends BffController {

  lazy val baseUrl = config.get("APP_BASE_URL")

  def pageFor(request: Request, urn: Urn): CursorBasedPage[Urn] =
    PageBuilder(request, baseUrl)(urn).buildCursorBased()

}
