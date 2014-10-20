package com.soundcloud.publicApiStrangler.support

import com.soundcloud.publicApiStrangler.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController
import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Response, Request}

trait TrackFiltering extends ContentAuthorizationComponent {
  self: BffController =>

  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)

  override val customFilters: Seq[Filter[Request, Response, Request, Response]] = List(authorizationFilter)
}
