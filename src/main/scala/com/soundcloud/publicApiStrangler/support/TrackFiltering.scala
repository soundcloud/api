package com.soundcloud.publicApiStrangler.support

import com.soundcloud.publicApiStrangler.authorization.{WaveformUrlsRepositoryComponent, AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController
import com.twitter.finagle.Filter
import com.twitter.finagle.http.{Response, Request}

trait TrackFiltering extends ContentAuthorizationComponent with WaveformUrlsRepositoryComponent {
  self: BffController =>

  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this, waveformUrlsRepo)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)
}
