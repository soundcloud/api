scalacOptions += "-Wconf:cat=deprecation:error"

addSbtPlugin("com.soundcloud" % "sbtkit" % "6.2.0")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "15.0.0")
