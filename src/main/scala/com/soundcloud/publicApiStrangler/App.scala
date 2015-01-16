package com.soundcloud.publicApiStrangler

import com.soundcloud.publicApiStrangler.support.TrackFiltering
import com.soundcloud.bff.{BazookaConfigComponent, BffApp}
import com.soundcloud.publicApiStrangler.controller.{GroupController, FallbackController, TimelineController}
import com.soundcloud.service.component.{GeoIpComponent, AuthenticatorComponent}
import com.soundcloud.publicApiStrangler.support.AcceptOnlyJsonRequestFilter

object App
    extends BazookaConfigComponent
    with TimelineController
    with FallbackController
    with GroupController
    with GeoIpComponent
    with AuthenticatorComponent
    with TrackFiltering
    with BffApp
{
  override val geoProvider = geoIpClient.get _
  override val authenticator = authenticatorClient.cacheKeyAndSession _
  override val customFilters = List(new AcceptOnlyJsonRequestFilter(Set("/crossdomain.xml")), authorizationFilter)
}
