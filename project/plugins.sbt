scalacOptions += "-Wconf:cat=deprecation:error"

addSbtPlugin("com.soundcloud" % "sbtkit" % "7.2.0")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "16.3.2")
