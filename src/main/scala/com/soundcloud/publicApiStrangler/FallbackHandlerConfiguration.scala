package com.soundcloud.publicApiStrangler

import com.soundcloud.jvmkit.module.telemetry.Telemetry
import com.soundcloud.jvmkit.module.util.Urn
import com.soundcloud.publicApiStrangler.handler.{DispatchToMothershipHandler, SpecificStranglingHandler}

class FallbackHandlerConfiguration(telemetry: Telemetry, mothershipDispatcher: DispatchToMothershipHandler) {
  private val officialSoundCloudApps = List(
    Urn("soundcloud", "applications", "46941"), // SoundCloud.com (currently being abused) Internal
    Urn("soundcloud", "applications", "124"), // SoundCloud iOS Internal
    Urn("soundcloud", "applications", "3152"), // SoundCloud Android Internal
    Urn("soundcloud", "applications", "3273"), // Mobile Soundcloud Internal
    Urn("soundcloud", "applications", "65097"), // Mobi (new mobile soundcloud) Internal
    Urn("soundcloud", "applications", "-1"), // Classic Internal
    Urn("soundcloud", "applications", "43164"), // SoundCloud Player Widget Internal
    Urn("soundcloud", "applications", "90575"), // SoundCloud Visual Embed Player Internal
    Urn("soundcloud", "applications", "60973"), // SoundCloud Flash Widget Internal
    Urn("soundcloud", "applications", "66151"), // Old mobi web Internal
    Urn("soundcloud", "applications", "3537"), // SoundCloud Desktop Internal
    Urn("soundcloud", "applications", "99561"), // SoundCloud Kik Messenger Card Internal
    Urn("soundcloud", "applications", "120502"), // Twitter Partner Internal
    Urn("soundcloud", "applications", "42975"), // SoundCloud Notifications Internal
    Urn("soundcloud", "applications", "147241"), // SoundCloud Jobs Page Internal
    Urn("soundcloud", "applications", "140141"), // SoundCloud Chromecast Receiver Internal
    Urn("soundcloud", "applications", "179522") // Facebook Partner Internal
  )

  private val fallthroughCounter = telemetry.counter(
    "fallthrough_strangled_by",
    "Fallthrough requests by the path pattern that strangles them",
    "method",
    "path_pattern",
    "agent_urn"
  )

  val fallbackHandler =
    new SpecificStranglingHandler(
      mothershipDispatcher.dispatch,
      List(".*".r),
      officialSoundCloudApps,
      fallthroughCounter
    )
}
