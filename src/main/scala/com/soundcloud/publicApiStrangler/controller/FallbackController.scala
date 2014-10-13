package com.soundcloud.publicApiStrangler.controller

import com.soundcloud.publicApiStrangler.authorization.{AuthorizeHttpResponse, ContentAuthorizationFilter}
import com.soundcloud.publicApiStrangler.support.{TrackFiltering, DispatchToMothershipHandler, PublicApiClientComponent}
import com.soundcloud.bff.ContentAuthorizationComponent
import com.soundcloud.bff.web.BffController


trait FallbackController extends BffController
  with PublicApiClientComponent {

  override val fallbackHandler = Some(new DispatchToMothershipHandler(publicApiClient))

}
