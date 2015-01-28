package com.soundcloud.publicApiStrangler.support

import com.soundcloud.bff.ContentAuthorizationComponent


trait TrackStreamSnipHandlerComponent extends PublicApiClientComponent
  with ContentAuthorizationComponent
  with MediaUrlsRepositoryComponent {

  lazy val mothershipDispatcher = new DispatchToMothershipHandler(publicApiClient)
  lazy val trackStreamSnipHandler = new TrackStreamSnipHandler(mothershipDispatcher, contentAuthorizationService, mediaUrlsRepository)

}
