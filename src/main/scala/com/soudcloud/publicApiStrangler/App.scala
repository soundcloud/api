package com.soudcloud.publicApiStrangler

import com.soudcloud.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.bff.{BazookaConfigComponent, BffApp, ContentAuthorizationComponent}
import com.soundcloud.bff.web.BffController

object App
    extends BazookaConfigComponent
    with BffApp
    with BffController
    with PublicApiClientComponent
    with ContentAuthorizationComponent {

  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)

  override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))

}
