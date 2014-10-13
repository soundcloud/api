package com.soudcloud.publicApiStrangler.controller

import com.soudcloud.publicApiStrangler.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soudcloud.publicApiStrangler.support.{DispatchToMothershipHandler, PublicApiClientComponent}
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController


trait FallbackController extends BffController
  with PublicApiClientComponent
  with ContentAuthorizationComponent {

  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)

  override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))

}
