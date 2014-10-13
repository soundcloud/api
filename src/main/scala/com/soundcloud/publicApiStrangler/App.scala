package com.soundcloud.publicApiStrangler

import com.soundcloud.publicApiStrangler.support.TrackFiltering
import com.soundcloud.bff.{BazookaConfigComponent, BffApp}
import com.soundcloud.publicApiStrangler.controller.{FallbackController, TimelineController}

object App
    extends BazookaConfigComponent
    with BffApp
    with TimelineController
    with FallbackController
