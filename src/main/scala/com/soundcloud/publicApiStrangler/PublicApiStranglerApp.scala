package com.soundcloud.publicApiStrangler

import com.soundcloud.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.bff._
import com.soundcloud.service.component.AuthenticatorComponent
import com.soundcloud.service.component.GeoIpComponent

object PublicApiStranglerApp extends BazookaConfigComponent
with BffApp
with PublicApiClientComponent
with ContentAuthorizationComponent
with GeoIpComponent
with AuthenticatorComponent {
  override val geoProvider: GeoProvider = geoIpClient.get
  override val authenticator: CacheKeyAndSessionProvider = authenticatorClient.cacheKeyAndSession

  val authorizeContent = new AuthorizeHttpResponse(contentAuthorizationService, this)
  val authorizationFilter = new ContentAuthorizationFilter(authorizeContent)
  override val fallbackHandler = Some(authorizationFilter andThen new DispatchToMothershipHandler(publicApiClient))
}
