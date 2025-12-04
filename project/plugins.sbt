scalacOptions += "-Wconf:cat=deprecation:error"

addSbtPlugin("com.soundcloud" % "sbtkit" % "8.6.1")
addSbtPlugin("com.soundcloud" % "jvmkit-twirp-sbt-plugin" % "17.3.6")
