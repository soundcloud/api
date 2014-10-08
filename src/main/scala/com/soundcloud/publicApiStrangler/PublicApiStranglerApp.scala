package com.soundcloud.publicApiStrangler

import com.soundcloud.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.bff.{BazookaConfigComponent, BffApp, ContentAuthorizationComponent}

object PublicApiStranglerApp extends BazookaConfigComponent
with BffApp
with PublicApiClientComponent
with ContentAuthorizationComponent {
  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)
  override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))
}
