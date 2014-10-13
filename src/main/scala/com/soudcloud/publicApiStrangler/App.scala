package com.soudcloud.publicApiStrangler

import com.soudcloud.publicApiStrangler.controller.{FallbackController, TimelineController}
import com.soundcloud.bff.{BazookaConfigComponent, BffApp}

object App
    extends BazookaConfigComponent
    with BffApp
    with TimelineController
    with FallbackController
